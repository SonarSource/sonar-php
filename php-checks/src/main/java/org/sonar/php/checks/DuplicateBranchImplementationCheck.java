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

import com.google.common.collect.Iterators;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Rule(
  key = DuplicateBranchImplementationCheck.KEY,
  name = "Two branches in the same conditional structure should not have exactly the same implementation",
  tags = {Tags.BUG},
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("10min")
public class DuplicateBranchImplementationCheck extends PHPVisitorCheck {

  public static final String KEY = "S1871";
  private static final String MESSAGE = "This %s's code block is the same as the block for the %s on line %s.";

  private List<IfStatementTree> checkedIfStatements;

  @Override
  public void visitScript(ScriptTree tree) {
    checkedIfStatements = new ArrayList<>();
    super.visitScript(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    super.visitSwitchStatement(tree);
    checkCasesForEquality(tree.cases());
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    if (tree.is(Kind.IF_STATEMENT) && !checkedIfStatements.contains(tree)) {

      List<StatementTree> branchesList = new ArrayList<>();
      branchesList.add(tree.statements().get(0));

      for (ElseifClauseTree elseifClauseTree : tree.elseifClauses()) {
        branchesList.add(elseifClauseTree.statements().get(0));
      }

      branchesList.addAll(getBranchesFromElse(tree.elseClause()));

      checkBranchesForEquality(branchesList);
    }

    super.visitIfStatement(tree);
  }

  private List<StatementTree> getBranchesFromElse(@Nullable ElseClauseTree elseClause) {
    List<StatementTree> branchesList = new ArrayList<>();
    if (elseClause != null) {
      ElseClauseTree currentElseClause = elseClause;

      while (currentElseClause != null) {
        StatementTree statement = currentElseClause.statements().get(0);

        if (statement.is(Kind.IF_STATEMENT)) {
          IfStatementTree nestedIfStatement = (IfStatementTree) statement;

          branchesList.add(nestedIfStatement.statements().get(0));
          checkedIfStatements.add(nestedIfStatement);
          currentElseClause = nestedIfStatement.elseClause();

        } else {
          branchesList.add(currentElseClause.statements().get(0));
          currentElseClause = null;
        }
      }
    }

    return branchesList;
  }

  private void checkBranchesForEquality(List<StatementTree> list) {
    for (int i = 1; i < list.size(); i++) {
      for (int j = 0; j < i; j++) {
        if (CheckUtils.areSyntacticallyEquivalent(list.get(i), list.get(j))) {
          raiseIssue("branch", list.get(j), list.get(i));
          break;
        }
      }
    }
  }

  private void checkCasesForEquality(List<SwitchCaseClauseTree> list) {
    for (int i = 1; i < list.size(); i++) {
      if (list.get(i).is(Kind.DEFAULT_CLAUSE)) {
        continue;
      }
      for (int j = 0; j < i; j++) {
        if (areSyntacticallyEquivalent(list.get(i).statements(), list.get(j).statements())) {
          raiseIssue("case", list.get(j), list.get(i));
          break;
        }
      }
    }
  }

  private void raiseIssue(String branchType, Tree duplicatedTree, Tree duplicatingTree) {
    String message = String.format(MESSAGE, branchType, branchType, ((PHPTree) duplicatedTree).getLine());
    context().newIssue(KEY, message).tree(duplicatingTree);
  }

  private static boolean areSyntacticallyEquivalent(List<StatementTree> list1, List<StatementTree> list2) {
    if (list1.isEmpty() && list2.isEmpty()) {
      return false;
    }
    return CheckUtils.areSyntacticallyEquivalent(Iterators.<Tree>concat(list1.<Tree>iterator()), Iterators.<Tree>concat(list2.iterator()));
  }
}
