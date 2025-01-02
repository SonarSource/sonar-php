/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.checks;

import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.type.FunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.php.checks.utils.type.TypePredicateList;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1155")
public class CountInsteadOfEmptyCheck extends PHPVisitorCheck {
  private static final TypePredicateList FUNCTION_PREDICATE = new TypePredicateList(
    new FunctionCall("count"),
    new FunctionCall("sizeof"));

  private static final Tree.Kind[] COMPARE_OPERATORS = {
    Tree.Kind.GREATER_THAN_OR_EQUAL_TO,
    Tree.Kind.GREATER_THAN,
    Tree.Kind.LESS_THAN_OR_EQUAL_TO,
    Tree.Kind.LESS_THAN,
    Tree.Kind.EQUAL_TO,
    Tree.Kind.NOT_EQUAL_TO,
    Tree.Kind.STRICT_EQUAL_TO,
    Tree.Kind.STRICT_NOT_EQUAL_TO
  };

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree argumentValue = CheckUtils.argument(tree, "array_or_countable", 0)
      .map(CallArgumentTree::value)
      .orElse(null);
    if (isCountFunction(tree) && isEmptyComparison(tree) && isArrayVariable(argumentValue)) {
      context().newIssue(this, tree.getParent(), "Use empty() to check whether the array is empty or not.");
    }
    super.visitFunctionCall(tree);
  }

  private static boolean isEmptyComparison(FunctionCallTree tree) {
    if (!tree.getParent().is(COMPARE_OPERATORS)) {
      return false;
    }

    BinaryExpressionTree parentBinaryTree = (BinaryExpressionTree) tree.getParent();

    boolean result;
    if (isEqualityExpression(parentBinaryTree)) {
      result = isZero(parentBinaryTree.leftOperand()) || isZero(parentBinaryTree.rightOperand());
    } else if (parentBinaryTree.is(Tree.Kind.GREATER_THAN) || parentBinaryTree.is(Tree.Kind.LESS_THAN_OR_EQUAL_TO)) {
      result = isOne(parentBinaryTree.leftOperand()) || isZero(parentBinaryTree.rightOperand());
    } else {
      // Kind.GREATER_THAN_OR_EQUAL_TO or Kind.LESS_THAN
      result = isZero(parentBinaryTree.leftOperand()) || isOne(parentBinaryTree.rightOperand());
    }

    return result;
  }

  private boolean isCountFunction(FunctionCallTree tree) {
    return FUNCTION_PREDICATE.test(TreeValues.of(tree, context().symbolTable()));
  }

  private boolean isArrayVariable(@Nullable ExpressionTree tree) {
    if (tree == null || !tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      return false;
    }

    if (CheckUtils.SUPERGLOBALS.contains(((VariableIdentifierTree) tree).variableExpression().text())) {
      return true;
    }

    Symbol symbol = context().symbolTable().getSymbol(tree);

    return symbol != null && (isSymbolUsedAsArray(symbol) || isSymbolArrayParameter(symbol));
  }

  private static boolean isSymbolUsedAsArray(Symbol symbol) {
    return symbol.usages().stream()
      .map(Tree::getParent)
      .map(Tree::getParent)
      .anyMatch(t -> t.is(Tree.Kind.ARRAY_ACCESS));
  }

  private static boolean isSymbolArrayParameter(Symbol symbol) {
    IdentifierTree declaration = symbol.declaration();

    if (!declaration.getParent().is(Tree.Kind.PARAMETER)) {
      return false;
    }

    DeclaredTypeTree parameterTypeTree = ((ParameterTree) declaration.getParent()).declaredType();
    if (parameterTypeTree == null
      || !parameterTypeTree.is(Tree.Kind.TYPE)
      || !((TypeTree) parameterTypeTree).typeName().is(Tree.Kind.BUILT_IN_TYPE)) {
      return false;
    }

    BuiltInTypeTree builtInType = ((BuiltInTypeTree) ((TypeTree) parameterTypeTree).typeName());

    return builtInType.token().text().equalsIgnoreCase("array");
  }

  private static boolean isEqualityExpression(BinaryExpressionTree tree) {
    return tree.is(Tree.Kind.EQUAL_TO,
      Tree.Kind.NOT_EQUAL_TO,
      Tree.Kind.STRICT_EQUAL_TO,
      Tree.Kind.STRICT_NOT_EQUAL_TO);
  }

  private static boolean isZero(ExpressionTree tree) {
    return tree.is(Tree.Kind.NUMERIC_LITERAL) &&
      "0".equals(((LiteralTree) tree).value());
  }

  private static boolean isOne(ExpressionTree tree) {
    return tree.is(Tree.Kind.NUMERIC_LITERAL) &&
      "1".equals(((LiteralTree) tree).value());
  }
}
