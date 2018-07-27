/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = LogicalWordOperatorUsageCheck.KEY)
public class LogicalWordOperatorUsageCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2010";
  public static final String MESSAGE = "Replace \"%s\" with \"%s\".";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
      Kind.ALTERNATIVE_CONDITIONAL_AND,
      Kind.ALTERNATIVE_CONDITIONAL_OR);
  }

  @Override
  public void visitNode(Tree tree) {
    BinaryExpressionTree binaryExpressionTree = (BinaryExpressionTree) tree;
    if (isCallToDie(binaryExpressionTree.rightOperand())) {
      return;
    }

    SyntaxToken operator = binaryExpressionTree.operator();
    String replacement = tree.is(Kind.ALTERNATIVE_CONDITIONAL_AND)
      ? PHPPunctuator.ANDAND.getValue()
      : PHPPunctuator.OROR.getValue();

    context().newIssue(this, operator, String.format(MESSAGE, operator.text(), replacement));
  }

  private static boolean isCallToDie(ExpressionTree tree) {
    return tree.is(Kind.FUNCTION_CALL) && "die".equals(CheckUtils.getFunctionName((FunctionCallTree) tree));
  }
}
