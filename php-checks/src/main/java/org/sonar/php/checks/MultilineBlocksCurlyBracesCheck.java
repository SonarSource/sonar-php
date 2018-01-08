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
import org.sonar.php.checks.utils.AbstractStatementsCheck;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = MultilineBlocksCurlyBracesCheck.KEY)
public class MultilineBlocksCurlyBracesCheck extends AbstractStatementsCheck {

  public static final String KEY = "S2681";

  private static final String MESSAGE_LOOP = "This statement will not be executed in a loop; only the first statement of this %s-statement block will be."
    + " The rest will execute only once.";

  private static final String MESSAGE_IF = "This statement will not be executed conditionally; only the first statement of this %s-statement block will be."
    + " The rest will execute unconditionally.";

  @Override
  public void visitNode(Tree tree) {
    List<StatementTree> statements = getStatements(tree);

    for (int i = 0; i < statements.size() - 1; i++) {
      StatementTree currentStatement = statements.get(i);
      if (currentStatement.is(Kind.IF_STATEMENT)) {
        IfStatementTree ifStatement = (IfStatementTree) currentStatement;
        checkStatement(ifStatement.ifToken(), ifStatement.condition(), getLastStatementOfIf(ifStatement), i, statements);
      } else if (currentStatement.is(Kind.FOR_STATEMENT)) {
        ForStatementTree forStatement = (ForStatementTree) currentStatement;
        checkStatement(forStatement.forToken(), forStatement.closeParenthesisToken(), forStatement.statements().get(0), i, statements);
      } else if (currentStatement.is(Kind.FOREACH_STATEMENT)) {
        ForEachStatementTree forStatement = (ForEachStatementTree) currentStatement;
        checkStatement(forStatement.foreachToken(), forStatement.closeParenthesisToken(), forStatement.statements().get(0), i, statements);
      } else if (currentStatement.is(Kind.WHILE_STATEMENT)) {
        WhileStatementTree whileStatement = (WhileStatementTree) currentStatement;
        checkStatement(whileStatement.whileToken(), whileStatement.condition(), whileStatement.statements().get(0), i, statements);
      }
    }
  }

  private static StatementTree getLastStatementOfIf(StatementTree statement) {
    if (!statement.is(Kind.IF_STATEMENT)) {
      return statement;
    }
    List<StatementTree> childStatements;
    IfStatementTree ifStatement = (IfStatementTree) statement;
    ElseClauseTree elseClause = ifStatement.elseClause();
    List<ElseifClauseTree> elseifClause = ifStatement.elseifClauses();
    if (elseClause != null) {
      childStatements = elseClause.statements();
    } else if (!elseifClause.isEmpty()) {
      childStatements = elseifClause.get(elseifClause.size() - 1).statements();
    } else {
      childStatements = ifStatement.statements();
    }
    return getLastStatementOfIf(childStatements.get(childStatements.size() - 1));
  }

  private void checkStatement(Tree parentStart, Tree parentEnd, StatementTree firstInnerStatement, int nestingStatementNum, List<StatementTree> statements) {
    if (firstInnerStatement.is(Kind.BLOCK)) {
      return;
    }
    int parentLine = lastTokenLine(parentEnd);
    int parentColumn = column(parentStart);
    StatementBlock statementBlock = findBlock(parentLine, parentColumn, firstInnerStatement, nestingStatementNum, statements);
    if (!statementBlock.otherStatement.isEmpty()) {
      boolean isIfStatement = statements.get(nestingStatementNum).is(Kind.IF_STATEMENT);
      String message = String.format(isIfStatement ? MESSAGE_IF : MESSAGE_LOOP, statementBlock.size());
      PreciseIssue issue = context().newIssue(this, statementBlock.otherStatement.get(0), message);
      issue.secondary(firstInnerStatement, isIfStatement ? "Executed conditionally" : "Executed in a loop");
      String secondaryMessage = isIfStatement ? "Always executed" : "Executed once";
      statementBlock.otherStatement.stream().skip(1).forEach(statement -> issue.secondary(statement, secondaryMessage));
    }
  }

  private static StatementBlock findBlock(int parentLine, int parentColumn, StatementTree firstInnerStatement, int nestingStatementNum,
    List<StatementTree> statements) {
    StatementBlock statementBlock = new StatementBlock(firstInnerStatement, parentColumn, parentLine);
    for (int i = nestingStatementNum + 1; i < statements.size(); i++) {
      StatementTree statement = statements.get(i);
      if (!statementBlock.add(statement)) {
        break;
      }
    }
    return statementBlock;
  }

  private static class StatementBlock {

    static final int MAX_STATEMENT_DISTANCE_IN_THE_SAME_BLOCK = 4;

    final StatementTree firstStatement;

    final List<StatementTree> otherStatement = new ArrayList<>();

    final int parentMarginColumn;

    int lastStatementLine;

    int lastStatementColumn;

    boolean firstStatementOneLiner;

    StatementBlock(StatementTree firstStatement, int parentMarginColumn, int parentLastLine) {
      this.firstStatement = firstStatement;
      lastStatementLine = line(firstStatement);
      this.firstStatementOneLiner = lastStatementLine == parentLastLine;
      lastStatementColumn = column(firstStatement);
      this.parentMarginColumn = parentMarginColumn;
    }

    int size() {
      return 1 + otherStatement.size();
    }

    /**
     * @return false if the statement is not part of the group
     */
    boolean add(StatementTree statement) {
      if (statement.is(Kind.INLINE_HTML)) {
        // ignore and continue
        return true;
      }
      int line = line(statement);
      int column = column(statement);
      if (column < parentMarginColumn || !isLineCloseToPreviousStatement(line)) {
        return false;
      } else if (column == parentMarginColumn &&
        (firstStatementOneLiner || column != lastStatementColumn || !isOneLineAfterPreviousStatement(line))) {
        return false;
      } else {
        otherStatement.add(statement);
        lastStatementLine = line;
        lastStatementColumn = column;
        return true;
      }
    }

    private boolean isLineCloseToPreviousStatement(int line) {
      return line < (lastStatementLine + MAX_STATEMENT_DISTANCE_IN_THE_SAME_BLOCK);
    }

    private boolean isOneLineAfterPreviousStatement(int line) {
      return line == (lastStatementLine + 1);
    }

    private static int line(Tree tree) {
      return ((PHPTree) tree).getFirstToken().line();
    }

  }

  private static int column(Tree tree) {
    return ((PHPTree) tree).getFirstToken().column();
  }

  private static int lastTokenLine(Tree tree) {
    return ((PHPTree) tree).getLastToken().line();
  }

}
