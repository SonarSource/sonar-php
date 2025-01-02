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

  private static final Set<String> SUSPICIOUS_TOKEN_VALUES = Set.of("!", "+", "-");

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
