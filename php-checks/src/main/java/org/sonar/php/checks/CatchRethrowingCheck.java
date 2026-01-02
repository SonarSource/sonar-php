/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = CatchRethrowingCheck.KEY)
public class CatchRethrowingCheck extends PHPVisitorCheck {
  public static final String KEY = "S2737";

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    List<CatchBlockTree> catchBlocks = tree.catchBlocks();
    if (catchBlocks.stream().allMatch(catchBlock -> hasSingleStatement(catchBlock) && isRetrowingException(catchBlock))) {
      catchBlocks.stream()
        .flatMap(catchBlock -> catchBlock.block().statements().stream())
        .forEach(statement -> context().newIssue(this, statement, "Add logic to this catch clause or eliminate it and rethrow the exception automatically."));
    }
    super.visitTryStatement(tree);
  }

  private static boolean hasSingleStatement(CatchBlockTree catchBlock) {
    return catchBlock.block().statements().size() == 1;
  }

  private static boolean isRetrowingException(CatchBlockTree catchBlock) {
    StatementTree statement = catchBlock.block().statements().get(0);
    if (!statement.is(Tree.Kind.THROW_STATEMENT) || catchBlock.variable() == null) {
      return false;
    }
    ExpressionTree thrownExpression = CheckUtils.skipParenthesis(((ThrowStatementTree) statement).expression());
    return SyntacticEquivalence.areSyntacticallyEquivalent(catchBlock.variable(), thrownExpression);
  }
}
