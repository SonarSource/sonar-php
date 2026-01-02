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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S3973")
public class ConditionalIndentationCheck extends PHPVisitorCheck {

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    checkIndentation(tree.ifToken(), tree.statements());
    super.visitIfStatement(tree);
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    checkIndentation(tree.whileToken(), tree.statements());
    super.visitWhileStatement(tree);
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    checkIndentation(tree.elseifToken(), tree.statements());
    super.visitElseifClause(tree);
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    checkIndentation(tree.elseToken(), tree.statements());
    super.visitElseClause(tree);
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    checkIndentation(tree.foreachToken(), tree.statements());
    super.visitForEachStatement(tree);
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    checkIndentation(tree.forToken(), tree.statements());
    super.visitForStatement(tree);
  }

  private void checkIndentation(SyntaxToken conditionalFirstToken, List<StatementTree> statements) {
    if (statements.isEmpty() || (statements.size() == 1 && statements.get(0).is(Tree.Kind.BLOCK))) {
      return;
    }
    StatementTree firstStatement = statements.get(0);
    SyntaxToken firstStatementToken = ((PHPTree) firstStatement).getFirstToken();
    if (conditionalFirstToken.column() == firstStatementToken.column()) {
      String message = "Use curly braces or indentation to denote the code conditionally executed by this \"" + conditionalFirstToken.text() + "\".";
      context()
        .newIssue(this, conditionalFirstToken, message)
        .secondary(firstStatementToken, null);
    }
  }

}
