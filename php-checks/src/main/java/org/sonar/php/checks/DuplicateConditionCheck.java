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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractDuplicateBranchCheck;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;

@Rule(key = DuplicateConditionCheck.KEY)
public class DuplicateConditionCheck extends AbstractDuplicateBranchCheck {

  public static final String KEY = "S1862";
  private static final String MESSAGE = "This %s duplicates the one on line %s.";

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);

    List<ExpressionTree> expressions = new ArrayList<>();

    for (SwitchCaseClauseTree switchCaseClauseTree : tree.cases()) {
      if (switchCaseClauseTree.is(Kind.CASE_CLAUSE)) {
        expressions.add(((CaseClauseTree) switchCaseClauseTree).expression());
      }
    }

    checkForEquality(expressions, "case");
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    if (tree.is(Kind.IF_STATEMENT) && !checkedIfStatements.contains(tree)) {

      List<ExpressionTree> conditionsList = new ArrayList<>();
      for (Tree clause : getClauses(tree)) {
        if (clause.is(Kind.IF_STATEMENT)) {
          conditionsList.add(((IfStatementTree) clause).condition());

        } else if (clause.is(Kind.ELSEIF_CLAUSE)) {
          conditionsList.add(((ElseifClauseTree) clause).condition());
        }
      }

      checkForEquality(conditionsList, "branch");
    }

    super.visitIfStatement(tree);
  }

  private void checkForEquality(List<ExpressionTree> list, String branchType) {
    for (int i = 1; i < list.size(); i++) {
      for (int j = 0; j < i; j++) {
        if (SyntacticEquivalence.areSyntacticallyEquivalent(list.get(i), list.get(j))) {
          raiseIssue(branchType, list.get(j), list.get(i));
          break;
        }
      }
    }
  }

  private void raiseIssue(String branchType, Tree duplicatedTree, Tree duplicatingTree) {
    String message = String.format(MESSAGE, branchType, ((PHPTree) duplicatedTree).getLine());
    context().newIssue(this, duplicatingTree, message).secondary(duplicatedTree, null);
  }

}
