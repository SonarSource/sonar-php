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
  protected void onAllEquivalentBranches(SyntaxToken keyword, List<List<StatementTree>> branches, boolean hasDefault, boolean hasFallthrough) {
    if (!hasFallthrough && hasDefault) {
      context().newIssue(this, keyword, MESSAGE);
    }
  }

  @Override
  protected void checkForDuplication(String branchType, List<List<StatementTree>> branchesList) {
    // do nothing, case handled by S1871
  }

}
