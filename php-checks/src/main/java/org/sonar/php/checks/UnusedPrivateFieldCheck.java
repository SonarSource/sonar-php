/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.HasAttributes;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.CallableConvertTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableVariableTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1068")
public class UnusedPrivateFieldCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Remove this unused \"%s\" private field.";

  private static final Set<String> constantUsedBeforeInit = new HashSet<>();
  private final PersistenceMapping persistenceMapping = new PersistenceMapping();

  @Override
  public void visitScript(ScriptTree tree) {
    persistenceMapping.reset();
    super.visitScript(tree);
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    persistenceMapping.enterNamespace(tree);
    super.visitNamespaceStatement(tree);
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    persistenceMapping.recordImports(tree);
    super.visitUseStatement(tree);
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    if (tree.is(Tree.Kind.CLASS_MEMBER_ACCESS) && tree.member().is(Tree.Kind.NAME_IDENTIFIER) && isSelfConstantAccess(tree.object())) {
      constantUsedBeforeInit.add(((NameIdentifierTree) tree.member()).text());
    }

    super.visitMemberAccess(tree);
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);

    MemberAccessCollector accesses = MemberAccessCollector.collect(tree, context().symbolTable());
    for (Symbol fieldSymbol : getFieldSymbolsForCurrentClass(tree)) {
      if (fieldSymbol.hasModifier("private") && fieldSymbol.usages().isEmpty() && !constantUsedBeforeInit.contains(fieldSymbol.name())
        && !persistenceMapping.isMappedProperty(fieldSymbol)
        && !accesses.mayAccess(fieldSymbol)) {
        context().newIssue(this, fieldSymbol.declaration(), String.format(MESSAGE, fieldSymbol.name()));
      }
    }

    constantUsedBeforeInit.clear();
  }

  private List<Symbol> getFieldSymbolsForCurrentClass(ClassDeclarationTree tree) {
    List<Tree.Kind> classDeclarationKind = Collections.singletonList(Tree.Kind.CLASS_DECLARATION);

    return context().symbolTable().getSymbols(Kind.FIELD).stream()
      .filter(f -> TreeUtils.findAncestorWithKind(f.declaration(), Collections.singletonList(Tree.Kind.ANONYMOUS_CLASS)) == null)
      .filter(f -> TreeUtils.findAncestorWithKind(f.declaration(), classDeclarationKind) == tree)
      .toList();
  }

  private static boolean isSelfConstantAccess(ExpressionTree tree) {
    if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
      String className = ((NamespaceNameTree) tree).fullName();
      if (TreeUtils.findAncestorWithKind(tree, Collections.singleton(Tree.Kind.ANONYMOUS_CLASS)) == null && className.equals("self")) {
        return true;
      }

      ClassDeclarationTree classDeclaration = (ClassDeclarationTree) TreeUtils.findAncestorWithKind(tree, Collections.singleton(Tree.Kind.CLASS_DECLARATION));
      return classDeclaration != null && classDeclaration.name().text().equals(className);
    }

    return false;
  }

  private static class MemberAccessCollector extends PHPVisitorCheck {
    private final ClassDeclarationTree containingClass;
    private final SymbolTable symbolTable;
    private final MemberNames instanceProperties = new MemberNames("$");
    private final MemberNames staticProperties = new MemberNames("$");
    private final MemberNames constants = new MemberNames("");

    private MemberAccessCollector(ClassDeclarationTree containingClass, SymbolTable symbolTable) {
      this.containingClass = containingClass;
      this.symbolTable = symbolTable;
    }

    static MemberAccessCollector collect(ClassDeclarationTree tree, SymbolTable symbolTable) {
      MemberAccessCollector accesses = new MemberAccessCollector(tree, symbolTable);
      accesses.scan(tree.members());
      return accesses;
    }

    /** Whether a computed access could resolve to this property or class constant. */
    boolean mayAccess(Symbol field) {
      Tree declaration = TreeUtils.findAncestorWithKind(field.declaration(),
        Tree.Kind.CLASS_PROPERTY_DECLARATION, Tree.Kind.CLASS_CONSTANT_PROPERTY_DECLARATION, Tree.Kind.PARAMETER);
      if (declaration == null) {
        return false;
      }
      if (declaration.is(Tree.Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)) {
        return constants.mayAccess(field.name());
      }
      MemberNames properties = field.hasModifier("static") ? staticProperties : instanceProperties;
      return properties.mayAccess(field.name());
    }

    @Override
    public void visitMemberAccess(MemberAccessTree tree) {
      super.visitMemberAccess(tree);
      if (isMethodAccess(tree)) {
        return;
      }
      if (tree.isStatic() && mayTargetCurrentClass(tree.object())) {
        // self::$name has a fixed property name; self::{$name} accesses a constant.
        if (tree.member() instanceof CompoundVariableTree compound) {
          staticProperties.add(compound.variableExpression());
        } else if (tree.member() instanceof VariableVariableTree) {
          staticProperties.add(tree.member());
        } else if (tree.member() instanceof ComputedVariableTree computed) {
          constants.add(computed.variableExpression());
        }
      } else if (!tree.isStatic() && isThis(tree.object()) && !(tree.member() instanceof NameIdentifierTree)) {
        Tree name = tree.member() instanceof ComputedVariableTree computed ? computed.variableExpression() : tree.member();
        instanceProperties.add(name);
      }
    }

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
      // Nested class members have their own class scope.
    }

    @Override
    public void visitAnonymousClass(AnonymousClassTree tree) {
      // Constructor arguments run in the enclosing class, while anonymous class members do not.
      scan(tree.callArguments());
    }

    private boolean mayTargetCurrentClass(ExpressionTree receiver) {
      // $this::${$name}
      if (isThis(receiver)) {
        return true;
      }
      // static::${$name}
      if (receiver instanceof NameIdentifierTree identifier) {
        return "static".equalsIgnoreCase(identifier.text());
      }
      // $className::${$name} or $other::${$name}: the receiver is unresolved.
      if (!(receiver instanceof NamespaceNameTree name)) {
        return false;
      }
      // self::${$name}
      if ("self".equalsIgnoreCase(name.fullName())) {
        return true;
      }
      // C::${$name}, \Ns\C::${$name}, or Alias::${$name}: resolve to the containing class.
      Symbol receiverSymbol = symbolTable.getSymbol(name.name());
      return receiverSymbol != null && receiverSymbol.declaration() == containingClass.name();
    }

    private static boolean isThis(ExpressionTree receiver) {
      return receiver instanceof VariableIdentifierTree variable && "$this".equals(variable.text());
    }

    private static boolean isMethodAccess(MemberAccessTree tree) {
      if (tree.getParent() instanceof CallableConvertTree callable && callable.expression() == tree) {
        return true;
      }
      return tree.getParent() instanceof FunctionCallTree call && call.callee() == tree && !(call.getParent() instanceof NewExpressionTree);
    }
  }

  private static class MemberNames {
    private final String symbolNamePrefix;
    private final Set<String> knownNames = new HashSet<>();
    private boolean hasUnresolvedDynamicAccess;

    MemberNames(String symbolNamePrefix) {
      this.symbolNamePrefix = symbolNamePrefix;
    }

    void add(Tree name) {
      if (name instanceof LiteralTree literal && literal.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        String literalSource = literal.value();
        // value() is raw PHP source, including the surrounding quotes. Without decoding
        // string escapes, presence of a backslash means that it contains escapes.
        // For simplicity, we don't implement string escaping as PHP would do, and just
        // bail out.
        if (literalSource.length() >= 2 && !literalSource.contains("\\")) {
          String unquotedName = literalSource.substring(1, literalSource.length() - 1);
          knownNames.add(symbolNamePrefix + unquotedName);
          return;
        }
      }
      hasUnresolvedDynamicAccess = true;
    }

    boolean mayAccess(String memberName) {
      return hasUnresolvedDynamicAccess || knownNames.contains(memberName);
    }
  }

  private static final class PersistenceMapping {
    private static final String DOCTRINE_MAPPING_NAMESPACE = "doctrine\\orm\\mapping\\";
    private static final Set<String> DOCTRINE_PROPERTY_MAPPINGS = Set.of(
      "column", "id", "generatedvalue", "sequencegenerator", "customidgenerator", "version", "embedded",
      "onetoone", "onetomany", "manytoone", "manytomany", "joincolumn", "joincolumns", "jointable", "orderby");

    private final Map<String, String> imports = new HashMap<>();
    private String currentNamespace = "";

    void reset() {
      imports.clear();
      currentNamespace = "";
    }

    void enterNamespace(NamespaceStatementTree statement) {
      imports.clear();
      currentNamespace = statement.namespaceName() == null ? "" : statement.namespaceName().qualifiedName();
    }

    void recordImports(UseStatementTree statement) {
      if (statement.useTypeToken() != null) {
        return;
      }
      String prefix = statement.prefix() == null ? "" : (statement.prefix().qualifiedName() + "\\");
      statement.clauses().stream()
        .filter(clause -> clause.useTypeToken() == null)
        .forEach(clause -> {
          String qualifiedName = prefix + clause.namespaceName().qualifiedName();
          String alias = clause.alias() == null ? clause.namespaceName().unqualifiedName() : clause.alias().text();
          imports.put(alias.toLowerCase(Locale.ROOT), qualifiedName.toLowerCase(Locale.ROOT));
        });
    }

    boolean isMappedProperty(Symbol symbol) {
      Tree declaration = symbol.declaration();
      while (declaration != null && !(declaration instanceof ClassPropertyDeclarationTree) && !declaration.is(Tree.Kind.PARAMETER)) {
        declaration = declaration.getParent();
      }
      if (declaration == null || declaration.is(Tree.Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)) {
        return false;
      }
      HasAttributes property = (HasAttributes) declaration;
      for (AttributeGroupTree group : property.attributeGroups()) {
        if (group.attributes().stream().anyMatch(attribute -> isDoctrineMapping(resolveAnnotation(attribute.name().fullName())))) {
          return true;
        }
      }
      return ((PHPTree) declaration).getFirstToken().trivias().stream()
        .anyMatch(trivia -> trivia.text().startsWith("/**") && hasMappingAnnotation(trivia.text()));
    }

    private boolean hasMappingAnnotation(String comment) {
      int start = 0;
      while ((start = comment.indexOf('@', start)) >= 0) {
        int end = start + 1;
        while (end < comment.length()) {
          char c = comment.charAt(end);
          if (Character.isLetterOrDigit(c) || c == '_' || c == '\\') {
            end++;
          } else {
            break;
          }
        }
        if (end > start + 1) {
          String name = comment.substring(start + 1, end);
          if (isDoctrineMapping(resolveAnnotation(name)) || isUnimportedDoctrineMapping(name)) {
            return true;
          }
        }
        start = end;
      }
      return false;
    }

    private boolean isUnimportedDoctrineMapping(String name) {
      // Doctrine also looks up unimported qualified annotation names as global class names.
      int separator = name.indexOf('\\');
      return separator > 0
        && !imports.containsKey(name.substring(0, separator).toLowerCase(Locale.ROOT))
        && isDoctrineMapping(name);
    }

    private String resolveAnnotation(String name) {
      String normalized = name.startsWith("\\") ? name.substring(1) : name;
      if (name.startsWith("\\")) {
        return normalized;
      }
      String namespacePrefix = "namespace\\";
      if (normalized.regionMatches(true, 0, namespacePrefix, 0, namespacePrefix.length())) {
        String relativeName = normalized.substring(namespacePrefix.length());
        return currentNamespace.isEmpty() ? relativeName : (currentNamespace + "\\" + relativeName);
      }
      int separator = normalized.indexOf('\\');
      String first = separator < 0 ? normalized : normalized.substring(0, separator);
      String imported = imports.get(first.toLowerCase(Locale.ROOT));
      if (imported != null) {
        return imported + (separator < 0 ? "" : normalized.substring(separator));
      }
      return currentNamespace.isEmpty() ? normalized : (currentNamespace + "\\" + normalized);
    }

    private boolean isDoctrineMapping(String name) {
      String normalized = (name.startsWith("\\") ? name.substring(1) : name).toLowerCase(Locale.ROOT);
      return normalized.startsWith(DOCTRINE_MAPPING_NAMESPACE)
        && DOCTRINE_PROPERTY_MAPPINGS.contains(normalized.substring(DOCTRINE_MAPPING_NAMESPACE.length()));
    }
  }

}
