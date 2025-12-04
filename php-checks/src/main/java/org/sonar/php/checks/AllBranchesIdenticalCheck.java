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
package org.sonar.php.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractDuplicateBranchImplementationCheck;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

@Rule(key = "S3923")
public class AllBranchesIdenticalCheck extends AbstractDuplicateBranchImplementationCheck {

  private static final String MESSAGE = "Remove this conditional structure or edit its code blocks so that they're not all the same.";

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    if (SyntacticEquivalence.areSyntacticallyEquivalent(tree.trueExpression(), tree.falseExpression())) {
      context().newIssue(this, tree.condition(), tree.queryToken(), MESSAGE);
    }
    super.visitConditionalExpression(tree);
  }

  @Override
  protected void reportAllDuplicateBranches(SyntaxToken keyword) {
    context().newIssue(this, keyword, MESSAGE);
  }

  @Override
  protected void reportTwoDuplicateBranches(String branchType, List<StatementTree> originalBranch, List<StatementTree> duplicateBranch) {
    // is handled by S1871 (DuplicateBranchImplementationCheck)
  }

}
