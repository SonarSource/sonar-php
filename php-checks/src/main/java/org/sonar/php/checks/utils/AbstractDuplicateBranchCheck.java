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
package org.sonar.php.checks.utils;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.utils.Preconditions;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public abstract class AbstractDuplicateBranchCheck extends PHPVisitorCheck {

  protected List<IfStatementTree> checkedIfStatements;

  @Override
  public void visitScript(ScriptTree tree) {
    checkedIfStatements = new ArrayList<>();
    super.visitScript(tree);
  }

  protected List<Tree> getClauses(IfStatementTree ifStatement) {
    Preconditions.checkArgument(ifStatement.is(Kind.IF_STATEMENT));

    List<Tree> clauses = new ArrayList<>();
    clauses.add(ifStatement);
    clauses.addAll(ifStatement.elseifClauses());

    ElseClauseTree currentElseClause = ifStatement.elseClause();

    while (currentElseClause != null) {
      StatementTree statement = currentElseClause.statements().get(0);

      if (statement.is(Kind.IF_STATEMENT)) {
        IfStatementTree nestedIfStatement = (IfStatementTree) statement;
        clauses.add(nestedIfStatement);
        checkedIfStatements.add(nestedIfStatement);
        currentElseClause = nestedIfStatement.elseClause();

      } else {
        clauses.add(currentElseClause);
        currentElseClause = null;
      }
    }

    return clauses;
  }

}
