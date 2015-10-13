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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.List;

@Rule(
  key = ElseIfWithoutElseCheck.KEY,
  name = "\"if ... else if\" constructs shall be terminated with an \"else\" clause",
  priority = Priority.MAJOR,
  tags = {Tags.CERT, Tags.MISRA})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class ElseIfWithoutElseCheck extends PHPVisitorCheck {

  public static final String KEY = "S126";
  private static final String MESSAGE = "Add the missing \"else\" clause.";

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);

    List<ElseifClauseTree> elseifClauses = tree.elseifClauses();

    if (!elseifClauses.isEmpty() && tree.elseClause() == null) {
      ElseifClauseTree lastElseIf = elseifClauses.get(elseifClauses.size() - 1);
      raiseIssue(lastElseIf);
    }
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    super.visitElseClause(tree);

    if (tree.is(Kind.ELSE_CLAUSE) && tree.statements().get(0).is(Kind.IF_STATEMENT)) {
      IfStatementTree nestedIf = (IfStatementTree)tree.statements().get(0);

      if (nestedIf.elseClause() == null && nestedIf.elseifClauses().isEmpty()) {
        raiseIssue(tree);
      }
    }
  }

  private void raiseIssue(Tree tree) {
    context().newIssue(KEY, MESSAGE).tree(tree);
  }

}
