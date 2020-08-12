/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.ClassSymbolIndex;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.FunctionSymbolData.FunctionSymbolProperties;
import org.sonar.php.symbols.FunctionSymbolIndex;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.MethodSymbolData;
import org.sonar.php.symbols.MethodSymbolImpl;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.symbols.UnknownLocationInFile;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.FunctionDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
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
  private final Map<ClassDeclarationTree, ClassSymbolData> classSymbolDataByTree = new HashMap<>();
  private ClassSymbolIndex classSymbolIndex;
  private final Map<ClassDeclarationTree, List<MethodSymbolData>> methodsByClassTree = new HashMap<>();
  private final Map<FunctionDeclarationTree, FunctionSymbolData> functionSymbolDataByTree = new HashMap<>();
  private FunctionSymbolIndex functionSymbolIndex;

  private ClassDeclarationTree currentClassTree;
  private Deque<FunctionSymbolProperties> functionPropertiesStack = new ArrayDeque<>();

  private static final Set<String> VALID_VISIBILITIES = ImmutableSet.of("PUBLIC", "PRIVATE", "PROTECTED");

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
      ((ClassDeclarationTreeImpl) declaration).setSymbol(symbol);
    });

    functionSymbolIndex = FunctionSymbolIndex.create(new HashSet<>(functionSymbolDataByTree.values()), projectSymbolData);
    functionSymbolDataByTree.forEach((declaration, symbolData) -> {
      FunctionSymbol symbol = functionSymbolIndex.get(symbolData);
      ((FunctionDeclarationTreeImpl) declaration).setSymbol(symbol);
    });
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    currentClassTree = tree;
    NamespaceNameTree superClass = tree.superClass();
    QualifiedName superClassName = superClass == null ? null : getFullyQualifiedName(superClass, Symbol.Kind.CLASS);

    List<QualifiedName> interfaceNames = tree.superInterfaces().stream()
      .map(name -> getFullyQualifiedName(name, Symbol.Kind.CLASS))
      .collect(Collectors.toList());

    IdentifierTree name = tree.name();
    SymbolQualifiedName qualifiedName = currentNamespace().resolve(name.text());

    symbolTable.declareTypeSymbol(tree.name(), globalScope, qualifiedName);
    super.visitClassDeclaration(tree);
    ClassSymbolData classSymbolData = new ClassSymbolData(location(name), qualifiedName, superClassName, interfaceNames,
      tree.is(Tree.Kind.INTERFACE_DECLARATION), methodsByClassTree.getOrDefault(tree, Collections.emptyList()));
    classSymbolDataByTree.put(tree, classSymbolData);

    currentClassTree = null;
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (currentClassTree == null) {
      super.visitMethodDeclaration(tree);
      return;
    }

    boolean backDidFindReturn = didFindReturn;
    didFindReturn = false;

    IdentifierTree name = tree.name();
    QualifiedName qualifiedName = QualifiedName.qualifiedName(name.text());

    List<Parameter> parameters = tree.parameters().parameters().stream()
      .map(Parameter::fromTree)
      .collect(Collectors.toList());

    String visibility = tree.modifiers().stream()
      .map(m -> m.text().toUpperCase(Locale.ROOT))
      .filter(VALID_VISIBILITIES::contains)
      .findFirst()
      .orElse("PUBLIC");

    super.visitMethodDeclaration(tree);

    MethodSymbolData methodSymbolData = new MethodSymbolData(location(name), qualifiedName, parameters, didFindReturn,
      Visibility.valueOf(visibility));
    ((MethodDeclarationTreeImpl) tree).setSymbol(new MethodSymbolImpl(methodSymbolData));
    methodsByClassTree.computeIfAbsent(currentClassTree, c -> new ArrayList<>()).add(methodSymbolData);

    didFindReturn = backDidFindReturn;
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
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    functionPropertiesStack.add(new FunctionSymbolProperties());
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
