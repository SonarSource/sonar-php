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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.ClassSymbolIndex;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.FunctionSymbolIndex;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.symbols.UnknownLocationInFile;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.php.tree.impl.declaration.FunctionDeclarationTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
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
  private final Map<FunctionDeclarationTree, FunctionSymbolData> functionSymbolDataByTree = new HashMap<>();
  private FunctionSymbolIndex functionSymbolIndex;

  private boolean didFindReturn;

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
    NamespaceNameTree superClass = tree.superClass();
    QualifiedName superClassName = superClass == null ? null : getFullyQualifiedName(superClass, Symbol.Kind.CLASS);

    List<QualifiedName> interfaceNames = tree.superInterfaces().stream()
      .map(name -> getFullyQualifiedName(name, Symbol.Kind.CLASS))
      .collect(Collectors.toList());

    IdentifierTree name = tree.name();
    SymbolQualifiedName qualifiedName = currentNamespace().resolve(name.text());
    classSymbolDataByTree.put(tree, new ClassSymbolData(location(name), qualifiedName, superClassName, interfaceNames, tree.is(Tree.Kind.INTERFACE_DECLARATION)));

    symbolTable.declareTypeSymbol(tree.name(), globalScope, qualifiedName);
    super.visitClassDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    boolean backDidFindReturn = didFindReturn;
    didFindReturn = false;

    symbolTable.declareSymbol(tree.name(), FUNCTION, globalScope, currentNamespace());

    IdentifierTree name = tree.name();
    SymbolQualifiedName qualifiedName = currentNamespace().resolve(name.text());

    List<FunctionSymbolData.Parameter> parameters = new ArrayList<>();
    tree.parameters().parameters().stream()
      .map(FunctionSymbolData.Parameter::fromTree)
      .forEach(parameters::add);

    super.visitFunctionDeclaration(tree);

    functionSymbolDataByTree.put(tree, new FunctionSymbolData(location(name), qualifiedName, parameters, didFindReturn));
    didFindReturn = backDidFindReturn;
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    didFindReturn = true;
    super.visitReturnStatement(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    boolean backDidFindReturn = didFindReturn;
    super.visitFunctionExpression(tree);
    didFindReturn = backDidFindReturn;
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

  private LocationInFile location(Tree tree) {
    if (filePath == null) {
      return UnknownLocationInFile.UNKNOWN_LOCATION;
    }
    SyntaxToken firstToken = ((PHPTree) tree).getFirstToken();
    SyntaxToken lastToken = ((PHPTree) tree).getLastToken();
    return new LocationInFileImpl(filePath, firstToken.line(), firstToken.column(), lastToken.endLine(), lastToken.endColumn());
  }

}
