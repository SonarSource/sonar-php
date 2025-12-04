/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.symbols;

import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.ClassSymbolIndex;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.FunctionSymbolData.FunctionSymbolProperties;
import org.sonar.php.symbols.FunctionSymbolIndex;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.symbols.UnknownLocationInFile;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.ReturnType;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.sonar.plugins.php.api.symbols.Symbol.Kind.FUNCTION;

public class DeclarationVisitor extends NamespaceNameResolvingVisitor {

  private final ProjectSymbolData projectSymbolData;
  @Nullable
  private final String filePath;
  private Scope globalScope;
  private final Map<ClassTree, ClassSymbolData> classSymbolDataByTree = new LinkedHashMap<>();
  private ClassSymbolIndex classSymbolIndex;
  private final Map<ClassTree, List<MethodSymbolData>> methodsByClassTree = new HashMap<>();
  private final Map<MethodSymbolData, MethodDeclarationTreeImpl> methodTreeByData = new HashMap<>();
  private final Map<FunctionDeclarationTree, FunctionSymbolData> functionSymbolDataByTree = new LinkedHashMap<>();
  private FunctionSymbolIndex functionSymbolIndex;

  private final Deque<ClassTree> classTreeStack = new ArrayDeque<>();
  private final Deque<FunctionSymbolProperties> functionPropertiesStack = new ArrayDeque<>();

  private static final Set<String> VALID_VISIBILITIES = Set.of("PUBLIC", "PRIVATE", "PROTECTED");
  private static final QualifiedName ANONYMOUS_CLASS_NAME = QualifiedName.qualifiedName("<anonymous_class>");

  DeclarationVisitor(SymbolTableImpl symbolTable, ProjectSymbolData projectSymbolData, @Nullable PhpFile file) {
    super(symbolTable);
    this.projectSymbolData = projectSymbolData;
    this.filePath = pathOf(file);
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    globalScope = symbolTable.addScope(new Scope(tree));
    super.visitCompilationUnit(tree);

    classSymbolIndex = ClassSymbolIndex.create(new ArrayList<>(classSymbolDataByTree.values()), projectSymbolData);
    classSymbolDataByTree.forEach((declaration, symbolData) -> {
      ClassSymbol symbol = classSymbolIndex.get(symbolData);
      ((HasClassSymbol) declaration).setSymbol(symbol);
      if (methodsByClassTree.containsKey(declaration)) {
        methodsByClassTree.get(declaration)
          .forEach(methodData -> methodTreeByData.get(methodData).setSymbol(symbol.getDeclaredMethod(methodData.name())));
      }
    });

    functionSymbolIndex = FunctionSymbolIndex.create(new ArrayList<>(functionSymbolDataByTree.values()), projectSymbolData);
    functionSymbolDataByTree.forEach((declaration, symbolData) -> {
      FunctionSymbol symbol = functionSymbolIndex.get(symbolData);
      ((HasFunctionSymbol) declaration).setSymbol(symbol);
    });
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    SymbolQualifiedName qualifiedName = currentNamespace().resolve(tree.name().text());
    symbolTable.declareTypeSymbol(tree.name(), globalScope, qualifiedName);
    classTreeStack.push(tree);
    super.visitClassDeclaration(tree);
    classTreeStack.pop();
    addClassSymbolData(tree, qualifiedName, location(tree.name()));
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    classTreeStack.push(tree);
    super.visitAnonymousClass(tree);
    classTreeStack.pop();
    addClassSymbolData(tree, ANONYMOUS_CLASS_NAME, location(tree.classToken()));
  }

  private void addClassSymbolData(ClassTree tree, QualifiedName qualifiedName, LocationInFile location) {
    NamespaceNameTree superClass = tree.superClass();
    QualifiedName superClassName = superClass == null ? null : getFullyQualifiedName(superClass, Symbol.Kind.CLASS);

    List<QualifiedName> interfaceNames = tree.superInterfaces().stream()
      .map(name -> getFullyQualifiedName(name, Symbol.Kind.CLASS))
      .toList();

    ClassSymbol.Kind kind = ClassSymbol.Kind.NORMAL;
    if (tree.is(Tree.Kind.CLASS_DECLARATION) && ((ClassDeclarationTree) tree).isAbstract()) {
      kind = ClassSymbol.Kind.ABSTRACT;
    } else if (tree.is(Tree.Kind.INTERFACE_DECLARATION)) {
      kind = ClassSymbol.Kind.INTERFACE;
    }

    ClassSymbolData classSymbolData = new ClassSymbolData(
      location,
      qualifiedName,
      superClassName,
      interfaceNames,
      kind,
      methodsByClassTree.getOrDefault(tree, Collections.emptyList()));
    classSymbolDataByTree.put(tree, classSymbolData);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    ClassTree currentClassTree = classTreeStack.peek();
    if (currentClassTree == null) {
      super.visitMethodDeclaration(tree);
      return;
    }

    functionPropertiesStack.push(new FunctionSymbolProperties());

    IdentifierTree name = tree.name();

    List<Parameter> parameters = tree.parameters().parameters().stream()
      .map(Parameter::fromTree)
      .toList();

    String visibilityName = tree.modifiers().stream()
      .map(m -> m.text().toUpperCase(Locale.ROOT))
      .filter(VALID_VISIBILITIES::contains)
      .findFirst()
      .orElse("PUBLIC");

    Visibility visibility = Visibility.valueOf(visibilityName);

    boolean isAbstract = tree.modifiers().stream()
      .map(m -> m.text().toUpperCase(Locale.ROOT))
      .anyMatch(m -> m.equals("ABSTRACT"));

    boolean isTestMethod = isTestMethod(tree, visibility);

    ReturnType returnType = SymbolReturnType.from(tree.returnTypeClause());

    super.visitMethodDeclaration(tree);

    MethodSymbolData methodSymbolData = new MethodSymbolData(location(name), name.text(), parameters,
      functionPropertiesStack.pop(), visibility, returnType, isAbstract, isTestMethod);

    methodTreeByData.put(methodSymbolData, (MethodDeclarationTreeImpl) tree);
    methodsByClassTree.computeIfAbsent(currentClassTree, c -> new ArrayList<>()).add(methodSymbolData);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    functionPropertiesStack.push(new FunctionSymbolProperties());

    symbolTable.declareSymbol(tree.name(), FUNCTION, globalScope, currentNamespace());

    var name = tree.name();
    var qualifiedName = currentNamespace().resolve(name.text());

    var parameters = tree.parameters().parameters().stream()
      .map(Parameter::fromTree)
      .toList();

    var returnType = SymbolReturnType.from(tree.returnTypeClause());

    super.visitFunctionDeclaration(tree);

    var data = new FunctionSymbolData(location(name), qualifiedName, parameters, functionPropertiesStack.pop(), returnType);
    functionSymbolDataByTree.put(tree, data);
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    functionSymbolProperties().ifPresent(p -> p.hasReturn(true));
    super.visitReturnStatement(tree);
  }

  @Override
  public void visitYieldExpression(YieldExpressionTree tree) {
    functionSymbolProperties().ifPresent(p -> p.hasReturn(true));
    super.visitYieldExpression(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    functionPropertiesStack.push(new FunctionSymbolProperties());
    super.visitFunctionExpression(tree);
    functionPropertiesStack.pop();
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    functionSymbolProperties().ifPresent(p -> {
      if (isFuncGetArgsCall(tree)) {
        p.hasFuncGetArgs(true);
      }
    });
    super.visitFunctionCall(tree);
  }

  public Collection<ClassSymbolData> classSymbolData() {
    return classSymbolDataByTree.values();
  }

  public ClassSymbolIndex classSymbolIndex() {
    return classSymbolIndex;
  }

  public Collection<FunctionSymbolData> functionSymbolData() {
    return functionSymbolDataByTree.values();
  }

  public FunctionSymbolIndex functionSymbolIndex() {
    return functionSymbolIndex;
  }

  private Optional<FunctionSymbolProperties> functionSymbolProperties() {
    if (!functionPropertiesStack.isEmpty()) {
      return Optional.of(functionPropertiesStack.getFirst());
    }
    return Optional.empty();
  }

  private LocationInFile location(Tree tree) {
    if (filePath == null) {
      return UnknownLocationInFile.UNKNOWN_LOCATION;
    }
    SyntaxToken firstToken = ((PHPTree) tree).getFirstToken();
    SyntaxToken lastToken = ((PHPTree) tree).getLastToken();
    return new LocationInFileImpl(filePath, firstToken.line(), firstToken.column(), lastToken.endLine(), lastToken.endColumn());
  }

  private static boolean isFuncGetArgsCall(FunctionCallTree fct) {
    return fct.callee().is(Tree.Kind.NAMESPACE_NAME)
      && ((NamespaceNameTree) fct.callee()).fullyQualifiedName().matches("func_get_arg(s)?");
  }

  /**
   * Provide the path of the file from the URI in case the PhpFile is located on the local filesystem
   */
  @CheckForNull
  protected static String pathOf(@Nullable PhpFile file) {
    if (file == null) {
      return null;
    }

    try {
      URI uri = file.uri();
      if ("file".equalsIgnoreCase(uri.getScheme())) {
        return Paths.get(uri).toString();
      }
      return null;
    } catch (InvalidPathException e) {
      return null;
    }
  }

  private boolean isTestMethod(MethodDeclarationTree methodTree, Visibility visibility) {
    if (!Visibility.PUBLIC.equals(visibility)) {
      return false;
    }

    Predicate<MethodDeclarationTree> hasTestNamePrefix = tree -> tree.name().text().startsWith("test");

    Predicate<MethodDeclarationTree> hasTestAnnotation = tree -> TreeUtils.hasAnnotation(tree, "@test");

    Predicate<MethodDeclarationTree> hasTestAttribute = tree -> tree.attributeGroups().stream()
      .flatMap(group -> group.attributes().stream()).map(AttributeTree::name)
      .anyMatch(nameTree -> "phpunit\\framework\\attributes\\test".equals(getFullyQualifiedName(nameTree, Symbol.Kind.CLASS).toString()));

    return hasTestNamePrefix.or(hasTestAnnotation).or(hasTestAttribute).test(methodTree);
  }
}
