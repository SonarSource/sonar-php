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
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
import org.sonar.php.utils.SymbolUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassNamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

import static org.sonar.php.utils.SymbolUtils.isSelfOrStatic;
import static org.sonar.php.utils.SymbolUtils.isThis;
import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAMESPACE_NAME;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAME_IDENTIFIER;
import static org.sonar.plugins.php.api.tree.Tree.Kind.OBJECT_MEMBER_ACCESS;

class SymbolUsageVisitor extends NamespaceNameResolvingVisitor {

  private final ClassSymbolIndex classSymbolIndex;
  private final FunctionSymbolIndex functionSymbolIndex;

  private final ArrayDeque<ClassSymbol> currentClassSymbolStack = new ArrayDeque<>();

  SymbolUsageVisitor(SymbolTableImpl symbolTable, ClassSymbolIndex classSymbolIndex, FunctionSymbolIndex functionSymbolIndex) {
    super(symbolTable);
    this.classSymbolIndex = classSymbolIndex;
    this.functionSymbolIndex = functionSymbolIndex;
  }

  @Override
  public void visitNamespaceName(NamespaceNameTree tree) {
    if (tree instanceof ClassNamespaceNameTree) {
      resolveClassSymbol((ClassNamespaceNameTree) tree);
    }
    super.visitNamespaceName(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    ExpressionTree callee = tree.callee();

    if (callee.is(NAMESPACE_NAME) && !SymbolUtils.isNewExpressionCall(tree)) {
      QualifiedName fqn = getFullyQualifiedName((NamespaceNameTree) callee, Symbol.Kind.FUNCTION);
      FunctionSymbol functionSymbol = functionSymbolIndex.get(fqn);
      ((FunctionCallTreeImpl) tree).setSymbol(functionSymbol);

    } else if (callee.is(OBJECT_MEMBER_ACCESS, CLASS_MEMBER_ACCESS)) {
      resolveMethodCall(tree, (MemberAccessTree) callee);
    }
  }

  private void resolveMethodCall(FunctionCallTree tree, MemberAccessTree callee) {
    ClassSymbol receiverSymbol = null;
    ExpressionTree object = callee.object();

    if (!currentClassSymbolStack.isEmpty() && (isSelfOrStatic(object) || isThis(object))) {
      receiverSymbol = currentClassSymbolStack.getFirst();
    } else if (callee.is(CLASS_MEMBER_ACCESS) && object.is(NAMESPACE_NAME)) {
      receiverSymbol = Symbols.get((ClassNamespaceNameTree) object);
    }

    if (receiverSymbol != null && callee.member().is(NAME_IDENTIFIER)) {
      String methodName = ((NameIdentifierTree) callee.member()).text();
      MethodSymbol methodSymbol = receiverSymbol.getDeclaredMethod(methodName);
      Optional<ClassSymbol> superClass = receiverSymbol.superClass();
      while (superClass.isPresent() && methodSymbol.isUnknownSymbol()) {
        methodSymbol = superClass.get().getDeclaredMethod(methodName);
        superClass = superClass.get().superClass();
      }
      ((FunctionCallTreeImpl) tree).setSymbol(methodSymbol);
    }
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
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

  private void resolveClassSymbol(ClassNamespaceNameTree namespaceName) {
    QualifiedName fqn = getFullyQualifiedName(namespaceName, Symbol.Kind.CLASS);
    ClassSymbol classSymbol = classSymbolIndex.get(fqn);
    ((ClassNamespaceNameTreeImpl) namespaceName).setSymbol(classSymbol);
  }

}
