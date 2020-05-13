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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.type.FunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.php.checks.utils.type.TypePredicateList;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1155")
public class CountInsteadOfEmptyCheck extends PHPVisitorCheck {
  private static final TypePredicateList FUNCTION_PREDICATE = new TypePredicateList(
    new FunctionCall("count"));

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
    if (isCountFunction(tree) && isInZeroCompare(tree) && isArray(tree.arguments().get(0))) {
      context().newIssue(this, tree, "Use empty() to check whether the array is empty or not.");
    }
    super.visitFunctionCall(tree);
  }

  private static boolean isInZeroCompare(FunctionCallTree tree) {
    Tree parent = tree.getParent();
    if (!parent.is(COMPARE_OPERATORS)) {
      return false;
    }

    BinaryExpressionTree comparisonTree = (BinaryExpressionTree) parent;
    ExpressionTree comparedValue;
    if (comparisonTree.leftOperand().equals(tree)) {
      comparedValue = comparisonTree.rightOperand();
    } else {
      comparedValue = comparisonTree.leftOperand();
    }

    return comparedValue.is(Tree.Kind.NUMERIC_LITERAL) && ((LiteralTree) comparedValue).value().equals("0");
  }

  private boolean isCountFunction(FunctionCallTree tree) {
    return FUNCTION_PREDICATE.test(TreeValues.of(tree, context().symbolTable())) && tree.arguments().size() == 1;
  }

  private boolean isArray(ExpressionTree tree) {
    Symbol symbol = context().symbolTable().getSymbol(tree);

    return isSymbolUsedAsArray(symbol) || isSymbolArrayParameter(symbol);
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

    TypeTree parameterTypeTree = ((ParameterTree) declaration.getParent()).type();
    if (parameterTypeTree == null || !parameterTypeTree.typeName().is(Tree.Kind.BUILT_IN_TYPE)) {
      return false;
    }

    BuiltInTypeTree builtInType = ((BuiltInTypeTree) parameterTypeTree.typeName());

    return builtInType.token().text().equalsIgnoreCase("array");
  }
}
