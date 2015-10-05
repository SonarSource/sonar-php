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

import com.google.common.collect.ImmutableList;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPTreeSubscriber;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = NonEmptyCaseWithoutBreakCheck.KEY,
  name = "Switch cases should end with an unconditional \"break\" statement",
  priority = Priority.CRITICAL,
  tags = {Tags.CERT, Tags.CWE, Tags.PITFALL, Tags.MISRA})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("10min")
public class NonEmptyCaseWithoutBreakCheck extends PHPVisitorCheck {

  public static final String KEY = "S128";

  private static final String MESSAGE = "End this switch case with an unconditional break, continue, return or throw statement.";

  @Override
  public void visitSwitchStatement(SwitchStatementTree switchTree) {
    SwitchCaseClauseTree currentClause = null;
    for (SwitchCaseClauseTree nextClause : switchTree.cases()) {
      if (currentClause != null && !isEmpty(currentClause) && !hasJumpStatement(currentClause) && !hasNoBreakComment(nextClause)) {
        context().newIssue(KEY, MESSAGE).tree(currentClause);
      }
      currentClause = nextClause;
    }
    super.visitSwitchStatement(switchTree);
  }

  private boolean hasNoBreakComment(SwitchCaseClauseTree caseClause) {
    return !caseClause.caseToken().trivias().isEmpty();
  }

  private static boolean hasJumpStatement(SwitchCaseClauseTree caseClause) {
    List<StatementTree> statements = caseClause.statements();
    if (statements.get(statements.size() - 1).is(Kind.BREAK_STATEMENT)) {
      return true;
    }
    return JumpStatementFinder.hasJumpStatement(caseClause);
  }

  private static boolean isEmpty(SwitchCaseClauseTree caseClause) {
    return caseClause.statements().isEmpty();
  }

  private static class JumpStatementFinder extends PHPTreeSubscriber {

    private boolean foundNode = false;

    @Override
    public List<Kind> nodesToVisit() {
      return ImmutableList.of(
        Kind.EXIT_EXPRESSION,
        Kind.CONTINUE_STATEMENT,
        Kind.THROW_STATEMENT,
        Kind.RETURN_STATEMENT,
        Kind.BREAK_STATEMENT,
        Kind.GOTO_STATEMENT);
    }

    @Override
    public void visitNode(Tree tree) {
      foundNode = true;
    }

    public static boolean hasJumpStatement(Tree tree) {
      JumpStatementFinder jumpStatementFinder = new JumpStatementFinder();
      jumpStatementFinder.scanTree(tree);
      return jumpStatementFinder.foundNode;
    }

  }

}
