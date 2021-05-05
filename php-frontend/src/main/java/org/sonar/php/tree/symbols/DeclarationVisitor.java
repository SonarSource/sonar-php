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

import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
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

  private final SymbolTableImpl symbolTable;
  private ProjectSymbolData projectSymbolData;
  @Nullable
  private String filePath;
  private Scope globalScope;
  private final Map<ClassTree, ClassSymbolData> classSymbolDataByTree = new HashMap<>();
  private ClassSymbolIndex classSymbolIndex;
  private final Map<ClassTree, List<MethodSymbolData>> methodsByClassTree = new HashMap<>();
  private final Map<MethodSymbolData, MethodDeclarationTreeImpl> methodTreeByData = new HashMap<>();
  private final Map<FunctionDeclarationTree, FunctionSymbolData> functionSymbolDataByTree = new HashMap<>();
  private FunctionSymbolIndex functionSymbolIndex;

  private Deque<ClassTree> classTreeStack = new ArrayDeque<>();
  private Deque<FunctionSymbolProperties> functionPropertiesStack = new ArrayDeque<>();

  private static final Set<String> VALID_VISIBILITIES = SetUtils.immutableSetOf("PUBLIC", "PRIVATE", "PROTECTED");
  private static final QualifiedName ANONYMOUS_CLASS_NAME = QualifiedName.qualifiedName("<anonymous_class>");

  DeclarationVisitor(SymbolTableImpl symbolTable, ProjectSymbolData projectSymbolData, @Nullable PhpFile file) {
    super(symbolTable);
    this.symbolTable = symbolTable;
    this.projectSymbolData = projectSymbolData;
    this.filePath = file == null ? null : Paths.get(file.uri()).toString();
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    globalScope = symbolTable.addScope(new Scope(tree));
    super.visitCompilationUnit(tree);

    classSymbolIndex = ClassSymbolIndex.create(new HashSet<>(classSymbolDataByTree.values()), projectSymbolData);
    classSymbolDataByTree.forEach((declaration, symbolData) -> {
      ClassSymbol symbol = classSymbolIndex.get(symbolData);
      ((HasClassSymbol) declaration).setSymbol(symbol);
      if (methodsByClassTree.containsKey(declaration)) {
        methodsByClassTree.get(declaration)
          .forEach(methodData -> methodTreeByData.get(methodData).setSymbol(symbol.getDeclaredMethod(methodData.name())));
      }
    });

    functionSymbolIndex = FunctionSymbolIndex.create(new HashSet<>(functionSymbolDataByTree.values()), projectSymbolData);
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
      .collect(Collectors.toList());

    ClassSymbol.Kind kind = ClassSymbol.Kind.NORMAL;
    if (tree.is(Tree.Kind.CLASS_DECLARATION) && ((ClassDeclarationTree)tree).modifierToken() != null) {
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
      methodsByClassTree.getOrDefault(tree, Collections.emptyList())
    );
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
      .collect(Collectors.toList());

    String visibility = tree.modifiers().stream()
      .map(m -> m.text().toUpperCase(Locale.ROOT))
      .filter(VALID_VISIBILITIES::contains)
      .findFirst()
      .orElse("PUBLIC");

    boolean isAbstract = tree.modifiers().stream()
      .map(m -> m.text().toUpperCase(Locale.ROOT))
      .anyMatch(m -> m.equals("ABSTRACT"));

    super.visitMethodDeclaration(tree);

    MethodSymbolData methodSymbolData = new MethodSymbolData(location(name), name.text(), parameters,
      functionPropertiesStack.pop(), Visibility.valueOf(visibility), isAbstract);

    methodTreeByData.put(methodSymbolData, (MethodDeclarationTreeImpl) tree);
    methodsByClassTree.computeIfAbsent(currentClassTree, c -> new ArrayList<>()).add(methodSymbolData);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    functionPropertiesStack.push(new FunctionSymbolProperties());

    symbolTable.declareSymbol(tree.name(), FUNCTION, globalScope, currentNamespace());

    IdentifierTree name = tree.name();
    SymbolQualifiedName qualifiedName = currentNamespace().resolve(name.text());

    List<Parameter> parameters = tree.parameters().parameters().stream()
      .map(Parameter::fromTree)
      .collect(Collectors.toList());

    super.visitFunctionDeclaration(tree);

    FunctionSymbolData data =  new FunctionSymbolData(location(name), qualifiedName, parameters, functionPropertiesStack.pop());
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
      if (isFuncGetArgsCall(tree)) { p.hasFuncGetArgs(true);}
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
}
