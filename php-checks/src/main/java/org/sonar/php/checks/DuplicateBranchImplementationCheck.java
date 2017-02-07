/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
import org.sonar.php.checks.utils.Equality;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;

@Rule(key = DuplicateBranchImplementationCheck.KEY)
public class DuplicateBranchImplementationCheck extends AbstractDuplicateBranchCheck {

  public static final String KEY = "S1871";
  private static final String MESSAGE = "This %s's code block is the same as the block for the %s on line %s.";
  private static final String MESSAGE_CONDITIONAL = "This conditional operation returns the same value whether the condition is \"true\" or \"false\".";

  private static class Branch {
    Tree clause;
    List<StatementTree> body;

    public Branch(Tree clause, List<StatementTree> body) {
      this.clause = clause;
      this.body = body;
    }
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    if (Equality.areSyntacticallyEquivalent(tree.trueExpression(), tree.falseExpression())) {
      context().newIssue(this, tree, MESSAGE_CONDITIONAL);
    }

    super.visitConditionalExpression(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);

    List<Branch> casesList = new ArrayList<>();

    for (SwitchCaseClauseTree clause : tree.cases()) {
      if (clause.is(Kind.CASE_CLAUSE)) {
        casesList.add(new Branch(clause, clause.statements()));
      }
    }

    checkForEquality("case", casesList);
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    if (tree.is(Kind.IF_STATEMENT) && !checkedIfStatements.contains(tree)) {

      List<Branch> branchesList = new ArrayList<>();

      for (Tree clause : getClauses(tree)) {
        if (clause.is(Kind.IF_STATEMENT)) {
          IfStatementTree ifStatementTree = (IfStatementTree) clause;
          branchesList.add(new Branch(clause, ifStatementTree.statements()));

        } else if (clause.is(Kind.ELSEIF_CLAUSE)) {
          ElseifClauseTree elseifClauseTree = (ElseifClauseTree) clause;
          branchesList.add(new Branch(clause, elseifClauseTree.statements()));

        } else if (clause.is(Kind.ELSE_CLAUSE)) {
          ElseClauseTree elseClause = (ElseClauseTree) clause;
          if (!elseClause.statements().get(0).is(Kind.IF_STATEMENT)) {
            branchesList.add(new Branch(clause, elseClause.statements()));
          }
        }
      }

      checkForEquality("branch", branchesList);
    }

    super.visitIfStatement(tree);
  }

  private void checkForEquality(String branchType, List<Branch> list) {
    for (int i = 1; i < list.size(); i++) {
      for (int j = 0; j < i; j++) {
        if (areSyntacticallyEquivalent(list.get(i).body, list.get(j).body)) {
          raiseIssue(branchType, list.get(j).clause, list.get(i).clause);
          break;
        }
      }
    }
  }

  private static boolean areSyntacticallyEquivalent(List<StatementTree> list1, List<StatementTree> list2) {
    boolean bothEmpty = list1.isEmpty() && list2.isEmpty();
    return !bothEmpty && Equality.areSyntacticallyEquivalent(list1.iterator(), list2.iterator());
  }

  @Override
  protected void raiseIssue(String branchType, Tree duplicatedTree, Tree duplicatingTree) {
    String message = String.format(MESSAGE, branchType, branchType, ((PHPTree) duplicatedTree).getLine());
    context().newIssue(this, duplicatingTree, message).secondary(duplicatedTree, null);
  }
}
