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
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2761")
public class RedundantComplementOperatorCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Use the \"{!|~}\" operator just once or not at all. ";

  private static final Kind[] COMPLEMENT_BINARY = {
    Kind.BITWISE_COMPLEMENT,
    Kind.LOGICAL_COMPLEMENT};

  private static final Kind[] BINARY_EXPRESSIONS = {
    Kind.CONCATENATION,
    Kind.POWER,
    Kind.MULTIPLY,
    Kind.DIVIDE,
    Kind.REMAINDER,
    Kind.PLUS,
    Kind.MINUS,
    Kind.LEFT_SHIFT,
    Kind.RIGHT_SHIFT,
    Kind.LESS_THAN,
    Kind.GREATER_THAN,
    Kind.LESS_THAN_OR_EQUAL_TO,
    Kind.GREATER_THAN_OR_EQUAL_TO,
    Kind.EQUAL_TO,
    Kind.STRICT_EQUAL_TO,
    Kind.NOT_EQUAL_TO,
    Kind.STRICT_NOT_EQUAL_TO,
    Kind.ALTERNATIVE_NOT_EQUAL_TO,
    Kind.COMPARISON,
    Kind.BITWISE_AND,
    Kind.BITWISE_XOR,
    Kind.BITWISE_OR,
    Kind.CONDITIONAL_AND,
    Kind.CONDITIONAL_OR,
    Kind.ALTERNATIVE_CONDITIONAL_AND,
    Kind.ALTERNATIVE_CONDITIONAL_XOR,
    Kind.ALTERNATIVE_CONDITIONAL_OR,
    Kind.NULL_COALESCING_EXPRESSION};

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(COMPLEMENT_BINARY)) {
      checkRedundantComplement(tree);
    }

    super.visitPrefixExpression(tree);
  }

  private void checkRedundantComplement(UnaryExpressionTree tree) {
    Tree parent = CheckUtils.getParentOfKind(tree, tree.getKind(), BINARY_EXPRESSIONS);
    if (parent != null) {
      context().newIssue(this, parent, MESSAGE);
    }
  }
}
