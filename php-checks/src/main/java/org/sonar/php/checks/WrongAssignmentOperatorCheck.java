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

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.expression.AssignmentExpressionTreeImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = "S2757")
public class WrongAssignmentOperatorCheck extends PHPSubscriptionCheck {

  private static final Set<String> SUSPICIOUS_TOKEN_VALUES = ImmutableSet.of("!", "+", "-");

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.ASSIGNMENT);
  }

  @Override
  public void visitNode(Tree tree) {
    AssignmentExpressionTree aeTree = (AssignmentExpressionTree) tree;
    SyntaxToken expressionFirstToken = ((PHPTree) aeTree.value()).getFirstToken();
    SyntaxToken variableLastToken = ((PHPTree) aeTree.variable()).getLastToken();
    SyntaxToken operatorToken = ((AssignmentExpressionTreeImpl) aeTree).equalToken();
    if (isSuspiciousToken(expressionFirstToken)
      && noSpacingBetween(operatorToken, expressionFirstToken)
      && !noSpacingBetween(variableLastToken, operatorToken)) {
      context().newIssue(this, operatorToken, expressionFirstToken, "Was \"" + expressionFirstToken.text() + "=\" meant instead?");
    }
  }

  private static boolean noSpacingBetween(SyntaxToken firstToken, SyntaxToken secondToken) {
    return firstToken.column() + firstToken.text().length() == secondToken.column();
  }

  private static boolean isSuspiciousToken(SyntaxToken firstToken) {
    return SUSPICIOUS_TOKEN_VALUES.contains(firstToken.text());
  }
}
