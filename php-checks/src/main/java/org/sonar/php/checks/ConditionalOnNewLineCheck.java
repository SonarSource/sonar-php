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

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = ConditionalOnNewLineCheck.KEY)
public class ConditionalOnNewLineCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S3972";

  private SyntaxToken previousClosingBracketToken;

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Arrays.asList(Tree.Kind.COMPILATION_UNIT, Tree.Kind.IF_STATEMENT, Tree.Kind.ELSEIF_CLAUSE, Tree.Kind.ELSE_CLAUSE);
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(Tree.Kind.COMPILATION_UNIT)) {
      previousClosingBracketToken = null;
      return;
    }
    if (tree.is(Tree.Kind.IF_STATEMENT) && previousClosingBracketToken != null) {
      SyntaxToken ifToken = ((IfStatementTree) tree).ifToken();
      if (ifToken.line() == previousClosingBracketToken.line()) {
        context()
          .newIssue(this, ifToken, "Move this \"if\" to a new line or add the missing \"else\".")
          .secondary(previousClosingBracketToken, null);
      }
    }
    setPreviousToken(tree);
  }

  private void setPreviousToken(Tree tree) {
    StatementTree statementTree = getConditionalStatement(tree);
    if (statementTree.is(Tree.Kind.BLOCK)) {
      BlockTree blockTree = (BlockTree) statementTree;
      if (isMultilineBlock(blockTree)) {
        previousClosingBracketToken = blockTree.closeCurlyBraceToken();
        return;
      }
    }
    previousClosingBracketToken = null;
  }

  private static boolean isMultilineBlock(BlockTree blockTree) {
    return blockTree.openCurlyBraceToken().line() != blockTree.closeCurlyBraceToken().line();
  }

  private static StatementTree getConditionalStatement(Tree tree) {
    List<StatementTree> statementTrees;
    if (tree.is(Tree.Kind.IF_STATEMENT)) {
      statementTrees = ((IfStatementTree) tree).statements();
    } else if (tree.is(Tree.Kind.ELSEIF_CLAUSE)) {
      statementTrees = ((ElseifClauseTree) tree).statements();
    } else {
      statementTrees = ((ElseClauseTree) tree).statements();
    }
    return statementTrees.get(0);
  }

}
