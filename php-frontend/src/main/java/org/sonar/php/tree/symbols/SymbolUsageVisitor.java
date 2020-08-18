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

import java.util.ArrayDeque;
import java.util.Optional;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolIndex;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.FunctionSymbolIndex;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;

import static org.sonar.php.utils.SymbolUtils.isFunctionCall;
import static org.sonar.php.utils.SymbolUtils.isMethodCall;
import static org.sonar.php.utils.SymbolUtils.isResolvableInnerMemberAccess;
import static org.sonar.php.utils.SymbolUtils.isResolvableMemberAccess;

class SymbolUsageVisitor extends NamespaceNameResolvingVisitor {

  private final ClassSymbolIndex classSymbolIndex;
  private final FunctionSymbolIndex functionSymbolIndex;

  private ArrayDeque<ClassSymbol> currentClassSymbolStack = new ArrayDeque<>();
  private boolean isInAnonymousClass;

  SymbolUsageVisitor(SymbolTableImpl symbolTable, ClassSymbolIndex classSymbolIndex, FunctionSymbolIndex functionSymbolIndex) {
    super(symbolTable);
    this.classSymbolIndex = classSymbolIndex;
    this.functionSymbolIndex = functionSymbolIndex;
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    for (NamespaceNameTree exceptionType : tree.exceptionTypes()) {
      resolveClassSymbol(exceptionType);
    }
    super.visitCatchBlock(tree);
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    ExpressionTree expression = tree.expression();
    if (expression.is(Tree.Kind.FUNCTION_CALL)) {
      expression = ((FunctionCallTree) expression).callee();
    }
    if (expression.is(Tree.Kind.NAMESPACE_NAME)) {
      resolveClassSymbol((NamespaceNameTree) expression);
    }
    super.visitNewExpression(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isFunctionCall(tree)) {
      resolveFunctionSymbol((NamespaceNameTree) tree.callee());
    } else if (isMethodCall(tree)) {
      ClassSymbol receiverSymbol = null;
      MemberAccessTree memberAccessTree = (MemberAccessTree) tree.callee();

      if (isResolvableMemberAccess(memberAccessTree)) {
        resolveClassSymbol((NamespaceNameTree) memberAccessTree.object());
        receiverSymbol = Symbols.getClass((NamespaceNameTree) memberAccessTree.object());
      } else if (!currentClassSymbolStack.isEmpty() && isResolvableInnerMemberAccess(memberAccessTree)) {
        receiverSymbol = currentClassSymbolStack.getFirst();
      }

      if (receiverSymbol != null) {
        resolveMethodSymbol((NameIdentifierTree) memberAccessTree.member(), receiverSymbol);
      }
    }

    super.visitFunctionCall(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    if (tree.superClass() != null) {
      resolveClassSymbol(tree.superClass());
    }
    currentClassSymbolStack.push(Symbols.get(tree));
    super.visitAnonymousClass(tree);
    currentClassSymbolStack.pop();
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    currentClassSymbolStack.push(Symbols.get(tree));
    super.visitClassDeclaration(tree);
    currentClassSymbolStack.pop();
  }

  private void resolveClassSymbol(NamespaceNameTree namespaceName) {
    QualifiedName fqn = getFullyQualifiedName(namespaceName, Symbol.Kind.CLASS);
    ClassSymbol classSymbol = classSymbolIndex.get(fqn);
    ((HasSymbol) namespaceName).setSymbol(classSymbol);
  }

  private void resolveFunctionSymbol(NamespaceNameTree namespaceNameTree) {
    QualifiedName fqn = getFullyQualifiedName(namespaceNameTree, Symbol.Kind.FUNCTION);
    FunctionSymbol functionSymbol = functionSymbolIndex.get(fqn);
    ((HasSymbol) namespaceNameTree).setSymbol(functionSymbol);
  }

  private void resolveMethodSymbol(NameIdentifierTree nameIdentifierTree, ClassSymbol receiverClassSymbol) {
    String methodName = nameIdentifierTree.text();
    MethodSymbol methodSymbol = receiverClassSymbol.getDeclaredMethod(methodName);
    Optional<ClassSymbol> superClass = receiverClassSymbol.superClass();
    while (superClass.isPresent() && methodSymbol.isUnknownSymbol()) {
      methodSymbol = superClass.get().getDeclaredMethod(methodName);
      superClass = superClass.get().superClass();
    }
    ((HasSymbol) nameIdentifierTree).setSymbol(methodSymbol);
  }
}
