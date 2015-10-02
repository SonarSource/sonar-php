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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = SwitchCaseTooBigCheck.KEY,
  name = "\"switch case\" clauses should not have too many lines",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
public class SwitchCaseTooBigCheck extends PHPVisitorCheck {

  public static final String KEY = "S1151";

  private static final String MESSAGE = "Reduce this \"switch/case\" number of lines from %s to at most %s, for example by extracting code into function.";

  public static final int DEFAULT = 10;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    SwitchCaseClauseTree previousClause = null;

    for (SwitchCaseClauseTree clause : tree.cases()) {
      if (previousClause != null) {
        checkCaseClause(previousClause, clause.caseToken().line());
      }
      previousClause = clause;
    }

    if (previousClause != null) {
      SyntaxToken nextToken = tree.closeCurlyBraceToken() == null ? tree.endswitchToken() : tree.closeCurlyBraceToken();
      checkCaseClause(previousClause, nextToken.line());
    }

    super.visitSwitchStatement(tree);
  }

  private void checkCaseClause(SwitchCaseClauseTree clause, int nextNodeLine) {
    int lines = nextNodeLine - clause.caseToken().line();
    if (lines > max) {
      context().newIssue(KEY, String.format(MESSAGE, lines, max)).tree(clause);
    }
  }
}
