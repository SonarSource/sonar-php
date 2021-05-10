/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.symbols;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.utils.SourceBuilder;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.symbols.MemberSymbol;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrowFunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CompoundVariableTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;

import static com.google.common.base.Preconditions.checkNotNull;

public class SymbolVisitor extends NamespaceNameResolvingVisitor {

  private static final Set<String> BUILT_IN_VARIABLES = SetUtils.immutableSetOf(
    "$this",
    "$GLOBALS",
    "$_SERVER",
    "$_GET",
    "$_POST",
    "$_FILES",
    "$_SESSION",
    "$_ENV",
    "$php_errormsg",
    "$HTTP_RAW_POST_DATA",
    "$http_response_header",
    "$_COOKIE",
    "$_REQUEST"
  );
  private static final Set<String> SELF_OBJECTS = SetUtils.immutableSetOf("$this", "self", "static");

  private Deque<Scope> classScopes = new ArrayDeque<>();
  private Map<Symbol, Scope> scopeBySymbol = new HashMap<>();

  static class ClassMemberUsageState {
    boolean isStatic = false;

    boolean isField = false;

    boolean isSelfMember = false;

    boolean isConst = false;
  }

  private SymbolTableImpl symbolTable;
  private Scope currentScope;
  private Scope globalScope;
  private ClassMemberUsageState classMemberUsageState = null;


  public SymbolVisitor(SymbolTableImpl symbolTable) {
    super(symbolTable);
    this.symbolTable = symbolTable;
    this.currentScope = null;
    this.globalScope = null;
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    globalScope = symbolTable.addScope(new Scope(tree));
    currentScope = globalScope;
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    enterScope(tree);

    super.visitNamespaceStatement(tree);

    if (isBracketedNamespace(tree)) {
      leaveScope();
    }
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    enterScope(tree);
    resolveDeclaredTypeNamespaceName(tree.returnTypeClause());
    super.visitFunctionDeclaration(tree);
    leaveScope();
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    enterScope(tree);
    super.visitFunctionExpression(tree);
    leaveScope();
  }

  @Override
  public void visitArrowFunctionExpression(ArrowFunctionExpressionTree tree) {
    enterArrowFunctionScope(tree);
    super.visitArrowFunctionExpression(tree);
    leaveScope();
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    enterScope(tree);
    resolveDeclaredTypeNamespaceName(tree.returnTypeClause());
    super.visitMethodDeclaration(tree);
    leaveScope();
  }

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    resolveDeclaredTypeNamespaceName(tree.declaredType());
  }

  @Override
  public void visitParameterList(ParameterListTree tree) {
    tree.parameters().forEach(t -> resolveDeclaredTypeNamespaceName(t.declaredType()));

    super.visitParameterList(tree);
  }

  private void resolveDeclaredTypeNamespaceName(@Nullable ReturnTypeClauseTree returnType) {
    if (returnType != null) {
      resolveDeclaredTypeNamespaceName(returnType.declaredType());
    }
  }

  private void resolveDeclaredTypeNamespaceName(@Nullable DeclaredTypeTree type) {
    if (type != null && type.isSimple() && ((TypeTree)type).typeName().is(Kind.NAMESPACE_NAME)) {
      lookupOrCreateUndeclaredSymbol((NamespaceNameTree) ((TypeTree)type).typeName());
    }
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    TypeSymbolImpl classSymbol = (TypeSymbolImpl) symbolTable.getSymbol(tree.name());
    checkNotNull(classSymbol, "Symbol for %s not found", tree);
    enterScope(tree);
    classScopes.push(currentScope);
    scan(tree.name());
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      Symbol superClassSymbol = lookupOrCreateUndeclaredSymbol(superClass);
      currentClassScope().superClassScope = scopeBySymbol.get(superClassSymbol);
      classSymbol.setSuperClass(superClassSymbol);
    }
    scopeBySymbol.put(classSymbol, currentClassScope());
    tree.superInterfaces().forEach(ifaceTree -> {
      Symbol ifaceSymbol = lookupOrCreateUndeclaredSymbol(ifaceTree);
      classSymbol.addInterface(ifaceSymbol);
    });
    createMemberSymbols(tree, classSymbol);
    scan(tree.members());
    classScopes.pop();
    leaveScope();
  }

  @Override
  public void visitUseTraitDeclaration(UseTraitDeclarationTree tree) {
    tree.traits().forEach(trait -> usageForNamespaceName(trait, Symbol.Kind.CLASS));
  }

  private Symbol lookupOrCreateUndeclaredSymbol(NamespaceNameTree superClass) {
    QualifiedName qualifiedName = getFullyQualifiedName(superClass, Symbol.Kind.CLASS);
    SymbolImpl superClassSymbol = (SymbolImpl) symbolTable.getSymbol(qualifiedName);
    if (superClassSymbol == null) {
      superClassSymbol = symbolTable.createUndeclaredSymbol(qualifiedName, Symbol.Kind.CLASS);
    }
    associateSymbol(superClass.name(), superClassSymbol);
    return superClassSymbol;
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    // the arguments are passed from the outer scope
    scan(tree.arguments());

    enterScope(tree);
    classScopes.push(currentScope);
    createMemberSymbols(tree, null);

    // we've already scanned the arguments
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      // because we don't have symbol for anonymous class we will not actually use this symbol
      lookupOrCreateUndeclaredSymbol(superClass);
    }
    // because we don't have symbol for anonymous class we will not actually use these symbols
    tree.superInterfaces().forEach(this::lookupOrCreateUndeclaredSymbol);
    scan(tree.members());

    classScopes.pop();
    leaveScope();
  }

  /**
   * @param classSymbol can be null for anonymous class
   */
  private void createMemberSymbols(ClassTree tree, @Nullable TypeSymbolImpl classSymbol) {
    for (ClassMemberTree member : tree.members()) {
      if (member.is(Kind.METHOD_DECLARATION)) {
        SymbolImpl symbol = createMemberSymbol(classSymbol, ((MethodDeclarationTree) member).name(), Symbol.Kind.FUNCTION);
        symbol.addModifiers(((MethodDeclarationTree) member).modifiers());
      } else if (member.is(Kind.CLASS_CONSTANT_PROPERTY_DECLARATION, Kind.CLASS_PROPERTY_DECLARATION)) {
        ClassPropertyDeclarationTree classPropertyDeclaration = (ClassPropertyDeclarationTree) member;
        for (VariableDeclarationTree field : classPropertyDeclaration.declarations()) {
          SymbolImpl symbol = createMemberSymbol(classSymbol, field.identifier(), Symbol.Kind.FIELD);
          symbol.addModifiers(classPropertyDeclaration.modifierTokens());
          ExpressionTree initValue = field.initValue();
          if (initValue != null) {
            initValue.accept(this);
          }
        }
      }
    }
  }

  private SymbolImpl createMemberSymbol(@Nullable TypeSymbolImpl classSymbol, IdentifierTree identifier, Symbol.Kind kind) {
    SymbolImpl symbol;
    if (classSymbol != null) {
      symbol = symbolTable.declareMemberSymbol(identifier, kind, currentClassScope(), classSymbol);
      classSymbol.addMember((MemberSymbol) symbol);
    } else {
      // anonymous class
      symbol = symbolTable.declareSymbol(identifier, kind, currentScope, currentNamespace());
    }
    return symbol;
  }

  @Override
  public void visitConstDeclaration(ConstantDeclarationTree tree) {
    for (VariableDeclarationTree constant : tree.declarations()) {
      createSymbol(constant.identifier(), Symbol.Kind.VARIABLE).addModifiers(Collections.singletonList(tree.constToken()));
    }
  }

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    if (!isBuiltInVariable(tree)) {
      if (classMemberUsageState == null) {
        createOrUseVariableIdentifierSymbol(tree);
      } else {

        if (classMemberUsageState.isSelfMember && isInClassScope() && classMemberUsageState.isStatic) {
          SymbolImpl symbol = (SymbolImpl) currentClassScope().getSymbol(tree.text(), Symbol.Kind.FIELD);
          if (symbol != null) {
            associateSymbol(tree, symbol);
          }
        }

        // see test_property_name_in_variable and case $this->$key ($key stores the name of field)
        SymbolImpl symbol = (SymbolImpl) currentScope.getSymbol(tree.text(), Symbol.Kind.VARIABLE, Symbol.Kind.PARAMETER);
        if (symbol != null) {
          associateSymbol(tree, symbol);
        }

        classMemberUsageState = null;
      }
    }
  }

  private void createOrUseVariableIdentifierSymbol(VariableIdentifierTree identifier) {
    SymbolImpl symbol = (SymbolImpl) currentScope.getSymbol(identifier.text(), Symbol.Kind.PARAMETER);
    if (symbol == null) {
      createSymbol(identifier, Symbol.Kind.VARIABLE);
      return;
    }
    associateSymbol(identifier, symbol);
  }


  @Override
  public void visitToken(SyntaxToken token) {
    if (classMemberUsageState != null && classMemberUsageState.isStatic && token.text().equals(PHPKeyword.CLASS.getValue())) {
      classMemberUsageState = null;
    }

    super.visitToken(token);
  }

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    if (classMemberUsageState != null && isInClassScope()) {
      resolveProperty(tree);
    } else {
      resolveSymbol(tree);
    }
    classMemberUsageState = null;
  }

  private Symbol resolveSymbol(IdentifierTree tree) {
    SymbolImpl symbol = null;
    Scope outer = currentScope;
    while (outer != null) {
      symbol = (SymbolImpl) outer.getSymbol(tree.text(), Symbol.Kind.CLASS);
      if (symbol != null) {
        associateSymbol(tree, symbol);
        break;
      }
      outer = outer.outer();
    }
    return symbol;
  }

  private void resolveProperty(NameIdentifierTree tree) {
    String name = tree.text();
    Symbol.Kind kind = Symbol.Kind.FUNCTION;
    if (classMemberUsageState.isField) {
      name = (classMemberUsageState.isConst ? "" : "$") + name;
      kind = Symbol.Kind.FIELD;
    }
    SymbolImpl symbol = (SymbolImpl) currentClassScope().getSymbol(name, kind);
    if (symbol != null) {
      associateSymbol(tree, symbol);
    }
  }

  private static boolean isBuiltInVariable(VariableIdentifierTree tree) {
    return BUILT_IN_VARIABLES.contains(tree.text());
  }

  @Override
  public void visitCompoundVariable(CompoundVariableTree tree) {
    SyntaxToken firstExpressionToken = ((PHPTree) tree.variableExpression()).getFirstToken();
    if (firstExpressionToken.text().charAt(0) != '$') {
      SymbolImpl symbol = (SymbolImpl) currentScope.getSymbol("$" + firstExpressionToken.text());
      if (symbol != null) {
        associateSymbol(firstExpressionToken, symbol);
      }
    }

    super.visitCompoundVariable(tree);
  }

  @Override
  public void visitParameter(ParameterTree tree) {
    createSymbol(tree.variableIdentifier(), Symbol.Kind.PARAMETER);
    ExpressionTree initValue = tree.initValue();
    if (initValue != null) {
      initValue.accept(this);
    }
    // do not scan the children to not pass through variableIdentifier
  }

  @Override
  public void visitGlobalStatement(GlobalStatementTree tree) {
    for (VariableTree variable : tree.variables()) {
      // Other cases are not supported
      if (variable.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        IdentifierTree identifier = (IdentifierTree) variable.variableExpression();
        SymbolImpl symbol = (SymbolImpl) globalScope.getSymbol(identifier.text(), Symbol.Kind.VARIABLE);
        if (symbol != null) {
          // actually this identifier in global statement is not usage, but we do this for the symbol highlighting
          associateSymbol(identifier, symbol);
          currentScope.addSymbol(symbol);
        } else {
          symbol = createSymbol(identifier, Symbol.Kind.VARIABLE);
        }
        // consider 'global' has being a modifier for the variable
        symbol.addModifiers(Collections.singletonList(tree.globalToken()));

      } else if (variable.is(Kind.COMPOUND_VARIABLE_NAME))  {
        visitCompoundVariable((CompoundVariableTree) variable);
      }
    }
  }

  @Override
  public void visitStaticStatement(StaticStatementTree tree) {
    // first visit declarations to create symbols which may not have been used yet
    super.visitStaticStatement(tree);
    // consider 'static' has being a modifier for the variables
    for (VariableDeclarationTree variable : tree.variables()) {
      // FIXME SONARPHP-741: can generate inconsistencies if the variable has already
      // been declared static in another statement, or if its global
      SymbolImpl symbol = (SymbolImpl) currentScope.getSymbol(variable.identifier().text(), Symbol.Kind.VARIABLE);
      if (symbol != null) {
        symbol.addModifiers(Collections.singletonList(tree.staticToken()));
      }
    }
  }

  @Override
  public void visitLexicalVariables(LexicalVariablesTree tree) {
    for (VariableTree variable : tree.variables()) {
      IdentifierTree identifier = null;
      if (variable.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        identifier = (IdentifierTree) variable.variableExpression();
      } else if (variable.is(Tree.Kind.REFERENCE_VARIABLE) && variable.variableExpression().is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        identifier = ((VariableIdentifierTree) variable.variableExpression()).variableExpression();
      }

      if (identifier != null) {
        SymbolImpl symbol = (SymbolImpl) currentScope.outer().getSymbol(identifier.text());
        if (symbol != null) {
          associateSymbol(identifier, symbol);
        } else if (variable.is(Kind.REFERENCE_VARIABLE)) {
          symbolTable.declareSymbol(identifier, Symbol.Kind.VARIABLE, currentScope.outer(), currentNamespace());
        }
        createSymbol(identifier, Symbol.Kind.VARIABLE);
      }
    }

  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    // syntax without parenthesis new A;
    if (tree.expression().is(Kind.NAMESPACE_NAME)) {
      usageForNamespaceName(((NamespaceNameTree) tree.expression()), Symbol.Kind.CLASS);
      return;
    }
    // syntax with parenthesis new A();
    if (tree.expression().is(Kind.FUNCTION_CALL)) {
      ExpressionTree callee = ((FunctionCallTree) tree.expression()).callee();
      if (callee.is(Kind.NAMESPACE_NAME)) {
        usageForNamespaceName(((NamespaceNameTree) callee), Symbol.Kind.CLASS);
        scan(((FunctionCallTree) tree.expression()).arguments());
        return;
      }
    }
    super.visitNewExpression(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (tree.callee().is(Kind.NAMESPACE_NAME)) {
      usageForNamespaceName((NamespaceNameTree) tree.callee(), Symbol.Kind.FUNCTION);
    } else {
      tree.callee().accept(this);
    }
    String callee = SourceBuilder.build(tree.callee()).trim();
    if ("compact".equals(callee) || "\\compact".equals(callee)) {
      visitCompactFunctionCall(tree.arguments());
    }

    scan(tree.arguments());
  }

  /**
   * See <a href="http://php.net/manual/en/function.compact.php">docs</a> of "compact" function
   *
   * @param arguments of call of "compact" function
   */
  private void visitCompactFunctionCall(SeparatedList<ExpressionTree> arguments) {
    for (ExpressionTree argument : arguments) {

      if (argument.is(Kind.REGULAR_STRING_LITERAL)) {
        String value = ((LiteralTree) argument).value();
        String variableName = "$" + value.substring(1, value.length() - 1);

        SymbolImpl symbol = (SymbolImpl) currentScope.getSymbol(variableName, Symbol.Kind.VARIABLE, Symbol.Kind.PARAMETER);

        if (symbol != null) {
          associateSymbol(((LiteralTree) argument).token(), symbol);
        }
      } else {
        // argument is array which can contain references to any variable
        currentScope.setUnresolvedCompact(true);
      }
    }
  }

  private void usageForNamespaceName(NamespaceNameTree namespaceName, Symbol.Kind kind) {
    if (namespaceName.name().is(Kind.NAME_IDENTIFIER)) {
      NameIdentifierTree usageIdentifier = (NameIdentifierTree) namespaceName.name();
      QualifiedName qualifiedName = getFullyQualifiedName(namespaceName, kind);
      SymbolImpl symbol = (SymbolImpl) symbolTable.getSymbol(qualifiedName);
      if (symbol == null && namespaceName.namespaces().isEmpty()) {
        symbol = (SymbolImpl) currentScope.getSymbol(usageIdentifier.text(), kind);
      }
      if (symbol == null) {
        // we do not have the declaration of this symbol, we will create unresolved symbol for it
        symbol = symbolTable.createUndeclaredSymbol(getFullyQualifiedName(namespaceName, kind), kind);
      }
      if (symbol != null) {
        associateSymbol(usageIdentifier, symbol);
      }
    }
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    boolean functionCall = tree.getParent().is(Kind.FUNCTION_CALL) && ((FunctionCallTree) tree.getParent()).callee() == tree;
    if (tree.object().is(Kind.NAMESPACE_NAME)) {
      usageForNamespaceName(((NamespaceNameTree) tree.object()), Symbol.Kind.CLASS);
    } else {
      tree.object().accept(this);
    }

    classMemberUsageState = new ClassMemberUsageState();
    classMemberUsageState.isStatic = tree.isStatic();
    classMemberUsageState.isSelfMember = isSelfMember(tree);
    classMemberUsageState.isField = !functionCall;
    classMemberUsageState.isConst = classMemberUsageState.isField && tree.isStatic();

    tree.member().accept(this);
  }

  private static boolean isSelfMember(MemberAccessTree tree) {
    String strObject = SourceBuilder.build(tree.object()).trim();
    return SELF_OBJECTS.contains(strObject.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public void visitComputedVariable(ComputedVariableTree tree) {
    classMemberUsageState = null;
    super.visitComputedVariable(tree);
  }

  private void leaveScope() {
    Preconditions.checkState(currentScope != null, "Current scope should never be null when calling method \"leaveScope\"");
    currentScope = currentScope.outer();
  }

  private void enterScope(Tree tree) {
    currentScope = symbolTable.addScope(new Scope(currentScope, tree, false));
  }

  private void enterArrowFunctionScope(Tree tree) {
    currentScope = symbolTable.addScope(new Scope(currentScope, tree, true));
  }

  private SymbolImpl createSymbol(IdentifierTree identifier, Symbol.Kind kind) {
    SymbolImpl symbol = (SymbolImpl) currentScope.getSymbol(identifier.text(), kind);

    if (symbol == null) {
      symbol = symbolTable.declareSymbol(identifier, kind, currentScope, currentNamespace());
      assignSymbolToIdentifier(identifier, symbol);
    } else {
      associateSymbol(identifier, symbol);
    }
    return symbol;
  }

  private void associateSymbol(IdentifierTree tree, SymbolImpl symbol) {
    symbol.addUsage(tree);
    symbolTable.associateSymbol(tree, symbol);
    assignSymbolToIdentifier(tree, symbol);
  }

  private static void assignSymbolToIdentifier(IdentifierTree tree, SymbolImpl symbol) {
    if (tree instanceof VariableIdentifierTree) {
      ((VariableIdentifierTreeImpl) tree).setSymbol(symbol);
    }
  }

  private void associateSymbol(SyntaxToken token, SymbolImpl symbol) {
    symbol.addUsage(token);
    symbolTable.associateSymbol(token, symbol);
  }

  private Scope currentClassScope() {
    return classScopes.peek();
  }

  private boolean isInClassScope() {
    return !classScopes.isEmpty();
  }

}
