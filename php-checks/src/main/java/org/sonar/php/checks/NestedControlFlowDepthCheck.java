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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = NestedControlFlowDepthCheck.KEY)
public class NestedControlFlowDepthCheck extends PHPVisitorCheck {

  public static final String KEY = "S134";

  private static final String MESSAGE = "Refactor this code to not nest more than %s \"if\", \"for\", \"while\", \"switch\" and \"try\" statements.";

  private Deque<Tree> nestingStack = new ArrayDeque<>();

  public static final int DEFAULT = 4;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    nestingStack.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    enterBlock(tree.ifToken());
    scanIf(tree);
    leaveBlock();
  }

  private void scanIf(IfStatementTree ifTree) {
    scan(ifTree.statements());
    scan(ifTree.elseifClauses());

    ElseClauseTree elseClause = ifTree.elseClause();
    if (elseClause != null) {
      List<StatementTree> elseStatements = elseClause.statements();
      if (elseStatements.size() == 1 && elseStatements.get(0).is(Kind.IF_STATEMENT)) {
        scanIf((IfStatementTree) elseStatements.get(0));
      } else {
        scan(elseClause);
      }
    }
  }

  @Override
  public void visitDoWhileStatement(DoWhileStatementTree tree) {
    enterBlock(tree.doToken());
    super.visitDoWhileStatement(tree);
    leaveBlock();
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    enterBlock(tree.forToken());
    super.visitForStatement(tree);
    leaveBlock();
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    enterBlock(tree.foreachToken());
    super.visitForEachStatement(tree);
    leaveBlock();
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    enterBlock(tree.switchToken());
    super.visitSwitchStatement(tree);
    leaveBlock();
  }

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    enterBlock(tree.tryToken());
    super.visitTryStatement(tree);
    leaveBlock();
  }

  @Override
  public void visitWhileStatement(WhileStatementTree tree) {
    enterBlock(tree.whileToken());
    super.visitWhileStatement(tree);
    leaveBlock();
  }

  private void enterBlock(Tree tree) {
    nestingStack.push(tree);
    if (nestingStack.size() == max + 1) {
      PreciseIssue issue = context().newIssue(this, tree, String.format(MESSAGE, max));
      nestingStack.forEach(secondaryTree -> issue.secondary(secondaryTree, "Nesting +1"));
    }
  }

  private void leaveBlock() {
    nestingStack.pop();
  }

}
