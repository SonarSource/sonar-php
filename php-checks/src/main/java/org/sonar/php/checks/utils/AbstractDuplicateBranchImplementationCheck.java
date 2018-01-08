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
package org.sonar.php.checks.utils;

import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;

public abstract class AbstractDuplicateBranchImplementationCheck extends AbstractDuplicateBranchCheck {

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    if (!tree.is(Kind.IF_STATEMENT) || checkedIfStatements.contains(tree)) {
      super.visitIfStatement(tree);
      return;
    }

    boolean hasElse = false;
    List<List<StatementTree>> branches = new ArrayList<>();

    for (Tree clause : getClauses(tree)) {
      if (clause.is(Kind.IF_STATEMENT)) {
        IfStatementTree ifStatementTree = (IfStatementTree) clause;
        branches.add(ifStatementTree.statements());

      } else if (clause.is(Kind.ELSEIF_CLAUSE)) {
        ElseifClauseTree elseifClauseTree = (ElseifClauseTree) clause;
        branches.add(elseifClauseTree.statements());

      } else if (clause.is(Kind.ELSE_CLAUSE)) {
        ElseClauseTree elseClause = (ElseClauseTree) clause;
        if (!elseClause.statements().get(0).is(Kind.IF_STATEMENT)) {
          branches.add(elseClause.statements());
          hasElse = true;
        }
      }
    }

    if (hasElse && areAllEquivalent(branches)) {
      onAllEquivalentBranches(tree.ifToken());
    } else {
      checkForDuplication("branch", branches);
    }

    super.visitIfStatement(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    boolean hasFallthrough = false;

    List<List<StatementTree>> normalizedBranches = new ArrayList<>();
    List<List<StatementTree>> caseBranches = new ArrayList<>();
    boolean hasDefault = false;
    int lastIndex = tree.cases().size() - 1;

    for (int i = 0; i < tree.cases().size(); i++) {
      SwitchCaseClauseTree switchCaseClause = tree.cases().get(i);
      List<StatementTree> statements = switchCaseClause.statements();

      if (i != lastIndex && !endsWithBreak(statements)) {
        hasFallthrough = true;
      }

      if (switchCaseClause.is(Kind.DEFAULT_CLAUSE)) {
        hasDefault = true;
      } else {
        caseBranches.add(statements);
      }

      normalizedBranches.add(normalize(statements));
    }

    if (hasDefault && !hasFallthrough && areAllEquivalent(normalizedBranches)) {
      onAllEquivalentBranches(tree.switchToken());
    } else {
      checkForDuplication("case", caseBranches);
    }

    super.visitSwitchStatement(tree);
  }

  private static List<StatementTree> normalize(List<StatementTree> statements) {
    if (endsWithBreak(statements)) {
      return statements.subList(0, statements.size() - 1);
    }
    return statements;
  }

  private static boolean endsWithBreak(List<StatementTree> statements) {
    return !statements.isEmpty() && statements.get(statements.size() - 1).is(Kind.BREAK_STATEMENT);
  }

  private static boolean areAllEquivalent(List<List<StatementTree>> branches) {
    List<StatementTree> firstBranch = branches.get(0);
    return branches.stream().allMatch(branch -> SyntacticEquivalence.areSyntacticallyEquivalent(firstBranch, branch));
  }

  protected abstract void onAllEquivalentBranches(SyntaxToken keyword);

  protected abstract void checkForDuplication(String branchType, List<List<StatementTree>> branchesList);
}
