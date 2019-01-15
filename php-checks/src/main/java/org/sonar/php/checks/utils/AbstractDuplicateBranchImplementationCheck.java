/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import java.util.stream.Stream;
import org.sonar.php.metrics.LineVisitor;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;

import static org.sonar.php.checks.utils.SyntacticEquivalence.areSyntacticallyEquivalent;

public abstract class AbstractDuplicateBranchImplementationCheck extends AbstractDuplicateBranchCheck {

  /**
   * "branches" should contain at least one element
   */
  private void checkBranches(String branchType, List<List<StatementTree>> branches, boolean reportAllDuplicate, SyntaxToken keywordToken) {
    if (areAllEquivalent(branches)) {
      if (reportAllDuplicate) {
        reportAllDuplicateBranches(keywordToken);

      } else if (!branches.get(0).isEmpty()) {
        // do not check size, but need at least one statement in branch to be able to report
        branches.stream().skip(1).forEach(branch -> reportTwoDuplicateBranches(branchType, branches.get(0), branch));
      }
    } else {
      for (int i = 1; i < branches.size(); i++) {
        for (int j = 0; j < i; j++) {
          List<StatementTree> originalBranch = branches.get(j);
          List<StatementTree> duplicateBranch = branches.get(i);
          if (areSyntacticallyEquivalent(duplicateBranch, originalBranch) && isNontrivial(duplicateBranch)) {
            reportTwoDuplicateBranches(branchType, originalBranch, duplicateBranch);
            break;
          }
        }
      }
    }
  }

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

      } else {
        branches.add(((ElseClauseTree) clause).statements());
        hasElse = true;
      }
    }

    // we don't want to report all duplicate branches for "if" without "else"
    // this means that in some case (implicit "else") nothing will be done
    boolean reportAllDuplicate = hasElse;
    checkBranches("branch", branches, reportAllDuplicate, tree.ifToken());

    super.visitIfStatement(tree);
  }

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    List<List<StatementTree>> normalizedBranches = new ArrayList<>();
    boolean hasDefault = false;
    boolean hasFallthrough = false;
    int lastIndex = tree.cases().size() - 1;

    for (int i = 0; i < tree.cases().size(); i++) {
      SwitchCaseClauseTree switchCaseClause = tree.cases().get(i);
      List<StatementTree> statements = switchCaseClause.statements();

      if (switchCaseClause.is(Kind.DEFAULT_CLAUSE)) {
        hasDefault = true;
      }

      if (!statements.isEmpty()) {
        normalizedBranches.add(normalize(statements));

        if (i != lastIndex && !endsWithBreak(statements)) {
          hasFallthrough = true;
        }
      }
    }

    if (!normalizedBranches.isEmpty()) {
      // we don't want to report all duplicate branches for "switch" without "default"
      // this means that in some case (no 'case' was matched) nothing will be done
      // same for "switch" with fallthrough, some branches will be fallen through, thus not identical
      boolean reportAllDuplicate = hasDefault && !hasFallthrough;
      checkBranches("case", normalizedBranches, reportAllDuplicate, tree.switchToken());
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
    return statements.get(statements.size() - 1).is(Kind.BREAK_STATEMENT);
  }

  private static boolean areAllEquivalent(List<List<StatementTree>> branches) {
    List<StatementTree> firstBranch = branches.get(0);
    return branches.stream().allMatch(branch -> areSyntacticallyEquivalent(firstBranch, branch));
  }

  private static boolean isNontrivial(List<StatementTree> statements) {
    return statements.stream()
      .flatMap(statement -> statement.is(Kind.BLOCK) ? ((BlockTree) statement).statements().stream() : Stream.of(statement))
      .mapToInt(LineVisitor::linesOfCode)
      .sum() > 1;
  }

  protected abstract void reportAllDuplicateBranches(SyntaxToken keyword);

  protected abstract void reportTwoDuplicateBranches(String branchType, List<StatementTree> originalBranch, List<StatementTree> duplicateBranch);

}
