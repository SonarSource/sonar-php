/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ReturnOfBooleanExpressionCheck.KEY)
public class ReturnOfBooleanExpressionCheck extends PHPVisitorCheck {

  public static final String KEY = "S1126";
  private static final String MESSAGE = "Replace this \"if-then-else\" statement by a single \"return\" statement.";

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);

    if (!tree.elseifClauses().isEmpty() || tree.elseClause() == null) {
      return;
    }

    if (returnsBoolean(getSingleStatement(tree.statements())) && returnsBoolean(getSingleStatement(tree.elseClause().statements()))) {
      context().newIssue(this, tree.ifToken(), tree.condition(), MESSAGE);
    }
  }

  private static boolean returnsBoolean(@Nullable StatementTree statement) {
    if (statement != null && statement.is(Kind.RETURN_STATEMENT)) {
      ReturnStatementTree returnStatement = (ReturnStatementTree) statement;

      if (returnStatement.expression() != null && returnStatement.expression().is(Kind.BOOLEAN_LITERAL)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  private static StatementTree getSingleStatement(List<StatementTree> statements) {
    if (statements.size() == 1) {

      if (statements.get(0).is(Kind.BLOCK)) {
        List<StatementTree> blockStatements = ((BlockTree) statements.get(0)).statements();

        if (blockStatements.size() == 1) {
          return blockStatements.get(0);
        }

      } else {
        return statements.get(0);
      }
    }

    return null;
  }
}
