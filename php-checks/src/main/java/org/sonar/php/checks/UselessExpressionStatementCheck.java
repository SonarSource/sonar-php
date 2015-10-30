/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = UselessExpressionStatementCheck.KEY,
  name = "Non-empty statements should change control flow or have at least one side-effect",
  priority = Priority.CRITICAL,
  tags = {Tags.UNUSED, Tags.CWE, Tags.MISRA, Tags.BUG})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("10min")
public class UselessExpressionStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S905";
  private static final String MESSAGE = "Remove or refactor this statement.";

  private static final Tree.Kind[] USELESS_KINDS = {
    Kind.FUNCTION_EXPRESSION,
    Kind.EQUAL_TO,
    Kind.STRICT_EQUAL_TO,
    Kind.NOT_EQUAL_TO,
    Kind.STRICT_NOT_EQUAL_TO,
    Kind.LESS_THAN,
    Kind.GREATER_THAN,
    Kind.LESS_THAN_OR_EQUAL_TO,
    Kind.GREATER_THAN_OR_EQUAL_TO,
    Kind.PLUS,
    Kind.MINUS,
    Kind.REMAINDER,
    Kind.MULTIPLY,
    Kind.DIVIDE,
    Kind.LEFT_SHIFT,
    Kind.RIGHT_SHIFT,
    Kind.INSTANCE_OF,
    Kind.ALTERNATIVE_NOT_EQUAL_TO,

    Kind.UNARY_MINUS,
    Kind.UNARY_PLUS,
    Kind.LOGICAL_COMPLEMENT,

    Kind.REGULAR_STRING_LITERAL,
    Kind.EXPANDABLE_STRING_LITERAL,
    Kind.CONCATENATION,
    Kind.NAME_IDENTIFIER,
    Kind.NUMERIC_LITERAL,
    Kind.NULL_LITERAL,
    Kind.BOOLEAN_LITERAL
  };

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    ExpressionTree expression = tree.expression();
    if (expression.is(USELESS_KINDS)) {
      context().newIssue(this, MESSAGE).tree(tree);
    }

    super.visitExpressionStatement(tree);
  }
}
