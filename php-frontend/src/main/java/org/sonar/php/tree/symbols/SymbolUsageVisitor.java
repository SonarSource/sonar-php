/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.symbols;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.ClassSymbolIndex;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.FunctionSymbolIndex;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.FunctionCallTreeImpl;
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
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;

import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAMESPACE_NAME;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NAME_IDENTIFIER;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NEW_EXPRESSION;
import static org.sonar.plugins.php.api.tree.Tree.Kind.OBJECT_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.VARIABLE_IDENTIFIER;

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
    if (tree instanceof ClassNamespaceNameTreeImpl classNamespaceName) {
      resolveClassSymbol(classNamespaceName);
    }
    super.visitNamespaceName(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    ExpressionTree callee = tree.callee();

    if (callee.is(NAMESPACE_NAME) && !isNewExpressionCall(tree)) {
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
      receiverSymbol = ((ClassNamespaceNameTreeImpl) object).symbol();
    }

    if (receiverSymbol != null && callee.member().is(NAME_IDENTIFIER)) {
      String methodName = ((NameIdentifierTree) callee.member()).text();
      MethodSymbol methodSymbol = receiverSymbol.getDeclaredMethod(methodName);
      Optional<ClassSymbol> superClass = receiverSymbol.superClass();
      Set<ClassSymbol> processedClasses = new HashSet<>();
      while (superClass.isPresent() && methodSymbol.isUnknownSymbol() && processedClasses.add(superClass.get())) {
        methodSymbol = superClass.get().getDeclaredMethod(methodName);
        superClass = superClass.get().superClass();
      }
      ((FunctionCallTreeImpl) tree).setSymbol(methodSymbol);
    }
  }

  public static boolean isNewExpressionCall(FunctionCallTree functionCallTree) {
    return functionCallTree.getParent() != null && functionCallTree.getParent().is(NEW_EXPRESSION);
  }

  public static boolean isThis(Tree object) {
    return object.is(VARIABLE_IDENTIFIER) && ((VariableIdentifierTree) object).text().equals("$this");
  }

  public static boolean isSelfOrStatic(Tree object) {
    return (object.is(NAMESPACE_NAME) && ((NamespaceNameTree) object).fullName().equals("self"))
      || (object.is(NAME_IDENTIFIER) && ((NameIdentifierTree) object).text().equals("static"));
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

  private void resolveClassSymbol(ClassNamespaceNameTreeImpl namespaceName) {
    QualifiedName fqn = getFullyQualifiedName(namespaceName, Symbol.Kind.CLASS);
    ClassSymbol classSymbol = classSymbolIndex.get(fqn);
    namespaceName.setSymbol(classSymbol);
  }

}
