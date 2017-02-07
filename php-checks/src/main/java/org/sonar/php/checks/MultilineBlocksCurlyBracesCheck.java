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
import org.sonar.php.checks.utils.AbstractStatementsCheck;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;

@Rule(key = MultilineBlocksCurlyBracesCheck.KEY)
public class MultilineBlocksCurlyBracesCheck extends AbstractStatementsCheck {

  public static final String KEY = "S2681";
  private static final String MESSAGE_LOOP = "Only the first line of this %s-line block will be executed in a loop. The rest will execute only once.";
  private static final String MESSAGE_IF = "Only the first line of this %s-line block will be executed conditionally. The rest will execute unconditionally.";

  @Override
  public void visitNode(Tree tree) {
    List<StatementTree> statements = getStatements(tree);

    for (int i = 0; i < statements.size() - 1; i++) {
      StatementTree currentStatement = statements.get(i);

      if (currentStatement.is(Kind.IF_STATEMENT)) {
        checkStatement(getLastStatement((IfStatementTree)currentStatement), i, statements);

      } else if (currentStatement.is(Kind.FOR_STATEMENT)) {
        checkStatement(((ForStatementTree) currentStatement).statements().get(0), i, statements);

      } else if (currentStatement.is(Kind.FOREACH_STATEMENT)) {
        checkStatement(((ForEachStatementTree) currentStatement).statements().get(0), i, statements);

      } else if (currentStatement.is(Kind.WHILE_STATEMENT)) {
        checkStatement(((WhileStatementTree) currentStatement).statements().get(0), i, statements);

      }

    }
  }

  private static StatementTree getLastStatement(IfStatementTree ifStatement) {
    if (ifStatement.elseClause() == null && ifStatement.elseifClauses().isEmpty()) {
      return ifStatement.statements().get(0);
    }

    StatementTree statement = ifStatement;

    do {
      IfStatementTree nestedIfStatement = (IfStatementTree) statement;

      if (nestedIfStatement.elseClause() != null) {
        statement = nestedIfStatement.elseClause().statements().get(0);

      } else {
        List<ElseifClauseTree> elseifClauses = nestedIfStatement.elseifClauses();
        if (elseifClauses.isEmpty()) {
          statement = nestedIfStatement.statements().get(0);

        } else {
          statement = elseifClauses.get(elseifClauses.size() - 1).statements().get(0);
        }
      }
    } while (statement.is(Kind.IF_STATEMENT));

    return statement;
  }

  private void checkStatement(StatementTree firstInnerStatement, int nestingStatementNum, List<StatementTree> statements) {
    if (firstInnerStatement.is(Kind.BLOCK)) {
      return;
    }

    int firstIndent = column(firstInnerStatement);
    List<StatementTree> statementsWhichShouldBeNested = new ArrayList<>();

    for (int i = nestingStatementNum + 1; i < statements.size(); i++) {
      StatementTree nextStatement = statements.get(i);

      if (!nextStatement.is(Kind.INLINE_HTML)) {

        if (column(nextStatement) == firstIndent) {
          statementsWhichShouldBeNested.add(nextStatement);

        } else {
          break;
        }
      }
    }

    if (!statementsWhichShouldBeNested.isEmpty()) {
      int firstInnerStatementLine = ((PHPTree) firstInnerStatement).getLine();
      int lastShouldBeNestedLine = ((PHPTree) statementsWhichShouldBeNested.get(statementsWhichShouldBeNested.size() - 1)).getLine();
      int blockSize = lastShouldBeNestedLine - firstInnerStatementLine + 1;
      String message = statements.get(nestingStatementNum).is(Kind.IF_STATEMENT) ? MESSAGE_IF : MESSAGE_LOOP;
      context().newIssue(this, statementsWhichShouldBeNested.get(0), String.format(message, blockSize));
    }
  }


  private static int column(Tree tree) {
    return ((PHPTree) tree).getFirstToken().column();
  }
}
