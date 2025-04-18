/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPTreeSubscriber;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = NonEmptyCaseWithoutBreakCheck.KEY)
public class NonEmptyCaseWithoutBreakCheck extends PHPVisitorCheck {

  public static final String KEY = "S128";

  private static final String MESSAGE = "End this switch case with an unconditional break, continue, return or throw statement.";

  @Override
  public void visitSwitchStatement(SwitchStatementTree switchTree) {
    SwitchCaseClauseTree currentClause = null;
    for (SwitchCaseClauseTree nextClause : switchTree.cases()) {
      if (currentClause != null && !isEmpty(currentClause) && !hasJumpStatement(currentClause) && !hasNoBreakComment(nextClause)) {
        context().newIssue(this, currentClause.caseToken(), currentClause.caseSeparatorToken(), MESSAGE);
      }
      currentClause = nextClause;
    }
    super.visitSwitchStatement(switchTree);
  }

  private static boolean hasNoBreakComment(SwitchCaseClauseTree caseClause) {
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
      return Arrays.asList(
        Kind.FUNCTION_CALL,
        Kind.CONTINUE_STATEMENT,
        Kind.THROW_STATEMENT,
        Kind.RETURN_STATEMENT,
        Kind.BREAK_STATEMENT,
        Kind.GOTO_STATEMENT);
    }

    @Override
    public void visitNode(Tree tree) {
      if (tree.is(Kind.FUNCTION_CALL) && !CheckUtils.isExitExpression((FunctionCallTree) tree)) {
        return;
      }

      foundNode = true;
    }

    public static boolean hasJumpStatement(Tree tree) {
      JumpStatementFinder jumpStatementFinder = new JumpStatementFinder();
      jumpStatementFinder.scanTree(tree);
      return jumpStatementFinder.foundNode;
    }

  }

}
