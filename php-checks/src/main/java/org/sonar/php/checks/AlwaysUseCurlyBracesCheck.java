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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = AlwaysUseCurlyBracesCheck.KEY)
public class AlwaysUseCurlyBracesCheck extends PHPVisitorCheck {

  public static final String KEY = "S121";
  private static final String MESSAGE = "Add curly braces around the nested statement(s).";

  private void checkStatement(StatementTree statementTree, Tree statementKeyword) {
    if (!statementTree.is(Tree.Kind.BLOCK) && !statementTree.is(Tree.Kind.EMPTY_STATEMENT)) {
      context().newIssue(this, statementKeyword, MESSAGE);
    }
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    super.visitIfStatement(tree);
    if (tree.is(Tree.Kind.IF_STATEMENT)) {
      checkStatement(tree.statements().get(0), tree.ifToken());
    }
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    super.visitElseifClause(tree);
    if (tree.is(Tree.Kind.ELSEIF_CLAUSE)) {
      checkStatement(tree.statements().get(0), tree.elseifToken());
    }
  }

  @Override
  public void visitElseClause(ElseClauseTree tree) {
    super.visitElseClause(tree);
    if (tree.is(Tree.Kind.ELSE_CLAUSE) && !tree.statements().get(0).is(Tree.Kind.IF_STATEMENT)) {
      checkStatement(tree.statements().get(0), tree.elseToken());
    }
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    super.visitForStatement(tree);
    if (tree.is(Tree.Kind.FOR_STATEMENT)) {
      checkStatement(tree.statements().get(0), tree.forToken());
    }
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    super.visitForEachStatement(tree);
    if (tree.is(Tree.Kind.FOREACH_STATEMENT)) {
      checkStatement(tree.statements().get(0), tree.foreachToken());
    }
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    super.visitDoWhileStatement(tree);
    checkStatement(tree.statement(), tree.doToken());
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    super.visitWhileStatement(tree);
    if (tree.is(Tree.Kind.WHILE_STATEMENT)) {
      checkStatement(tree.statements().get(0), tree.whileToken());
    }
  }

}
