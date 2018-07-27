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
package org.sonar.php.checks.formatting;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.checks.utils.TokenVisitor;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

public class ControlStructureSpacingCheck extends PHPSubscriptionCheck implements FormattingCheck {

  private static final String CONTROL_STRUCTURES_KEYWORD_MESSAGE = "between this \"%s\" keyword and the opening %s";
  private static final String FOR_SEMICOLON_MESSAGE = "Put exactly one space after each \";\" character in the \"for\" statement.";
  private static final String FOREACH_MESSAGE = "Put exactly one space after and before %s in \"foreach\" statement.";
  private static final Kind[] CONTROL_STRUCTURES = {
    Kind.IF_STATEMENT,
    Kind.ELSEIF_CLAUSE,
    Kind.ELSE_CLAUSE,
    Kind.DO_WHILE_STATEMENT,
    Kind.WHILE_STATEMENT,
    Kind.FOR_STATEMENT,
    Kind.FOREACH_STATEMENT,
    Kind.SWITCH_STATEMENT,
    Kind.TRY_STATEMENT,
    Kind.CATCH_BLOCK
  };

  private FormattingStandardCheck check;

  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;
    this.scanTree(scriptTree);
  }

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.copyOf(CONTROL_STRUCTURES);
  }

  @Override
  public void visitNode(Tree tree) {
    if (check.isOneSpaceBetweenKeywordAndNextToken) {
      checkSpaceBetweenKeywordAndNextNode(new TokenVisitor(tree), tree);
    }
    if (check.isOneSpaceAfterForLoopSemicolon && tree.is(Kind.FOR_STATEMENT)) {
      checkSpaceForStatement(tree);
    }
    if (check.isSpaceForeachStatement && tree.is(Kind.FOREACH_STATEMENT)) {
      ForEachStatementTree foreachLoop = (ForEachStatementTree) tree;
      checkForeachStatement(new TokenVisitor(tree), foreachLoop, foreachLoop.asToken(), foreachLoop.doubleArrowToken());
    }
  }

  /**
   * Check there is exactly one space around "as" keyword and "=>" punctuator in foreach statement.
   */
  private void checkForeachStatement(TokenVisitor tokenVisitor, ForEachStatementTree foreachLoop, SyntaxToken asKeyword, @Nullable SyntaxToken doubleArrow) {
    boolean isSpaceCorrectAs = isExactlyOneSpaceAroundOrLineSplit(tokenVisitor, asKeyword);
    boolean isSpaceCorrectDoubleArrow = doubleArrow == null || isExactlyOneSpaceAroundOrLineSplit(tokenVisitor, doubleArrow);

    String messageDetail = null;
    List<Tree> issueLocations = new ArrayList<>();

    if (!isSpaceCorrectAs && !isSpaceCorrectDoubleArrow) {
      messageDetail = "\"as\" and \"=>\"";
      issueLocations.add(foreachLoop.asToken());
      issueLocations.add(foreachLoop.doubleArrowToken());
    } else if (!isSpaceCorrectAs) {
      messageDetail = "\"as\"";
      issueLocations.add(foreachLoop.asToken());
    } else if (!isSpaceCorrectDoubleArrow) {
      messageDetail = "\"=>\"";
      issueLocations.add(foreachLoop.doubleArrowToken());
    }

    if (messageDetail != null) {
      check.reportIssue(String.format(FOREACH_MESSAGE, messageDetail), issueLocations.toArray(new Tree[issueLocations.size()]));
    }
  }

  private static boolean isExactlyOneSpaceAroundOrLineSplit(TokenVisitor tokenVisitor, SyntaxToken token) {
    SyntaxToken prevToken = tokenVisitor.prevToken(token);
    SyntaxToken nextToken = tokenVisitor.nextToken(token);
    return isExactlyOneSpaceBetweenOrLineSplit(prevToken, token) && isExactlyOneSpaceBetweenOrLineSplit(token, nextToken);
  }

  private static boolean isExactlyOneSpaceBetweenOrLineSplit(SyntaxToken leftToken, SyntaxToken rightToken) {
    return TokenUtils.getNbSpaceBetween(leftToken, rightToken) == 1 || !TokenUtils.isOnSameLine(leftToken, rightToken);
  }

  /**
   * Check there is exactly one space after each ";" in for statement.
   */
  private void checkSpaceForStatement(Tree tree) {
    Iterator<Tree> iterator = ((PHPTree) tree).childrenIterator();
    Tree next;
    Tree previous = null;

    while (iterator.hasNext()) {
      next = iterator.next();

      if (isSemicolon(previous)) {
        SyntaxToken semicolonToken = (SyntaxToken) previous;
        SyntaxToken nextToken = ((PHPTree) next).getFirstToken();
        int nbSpace = TokenUtils.getNbSpaceBetween(semicolonToken, nextToken);

        if (nbSpace != 1 && TokenUtils.isOnSameLine(semicolonToken, nextToken)) {
          check.reportIssue(FOR_SEMICOLON_MESSAGE, semicolonToken);
          break;
        }
      }

      previous = next;
    }

  }

  private static boolean isSemicolon(@Nullable Tree tree) {
    return tree != null && tree.is(Kind.TOKEN) && TokenUtils.isType((SyntaxToken) tree, PHPPunctuator.SEMICOLON);
  }

  private void checkSpaceBetweenKeywordAndNextNode(TokenVisitor tokenVisitor, Tree tree) {
    SyntaxToken keyword = tokenVisitor.firstKeyword();

    if (tree.is(Kind.TRY_STATEMENT)) {
      TryStatementTree tryStatement = (TryStatementTree) tree;

      if (tryStatement.finallyToken() != null) {
        SyntaxToken finallyKeyword = tryStatement.finallyToken();
        checkSpaceBetweenKeywordAndNextNode(finallyKeyword, tokenVisitor.nextToken(finallyKeyword));
      }
    }

    checkSpaceBetweenKeywordAndNextNode(keyword, tokenVisitor.nextToken(keyword));
  }

  /**
   * Check that there is exactly one space between a control structure keyword and a opening parenthesis or curly brace.
   */
  private void checkSpaceBetweenKeywordAndNextNode(SyntaxToken keyword, SyntaxToken nextToken) {
    if (TokenUtils.isType(nextToken, PHPPunctuator.LCURLYBRACE, PHPPunctuator.LPARENTHESIS) && TokenUtils.isOnSameLine(keyword, nextToken)) {
      int nbSpace = TokenUtils.getNbSpaceBetween(keyword, nextToken);

      if (nbSpace != 1) {
        String endMessage = String.format(
          CONTROL_STRUCTURES_KEYWORD_MESSAGE,
          keyword.text(),
          TokenUtils.isType(nextToken, PHPPunctuator.LPARENTHESIS) ? "parenthesis." : "curly brace.");
        check.reportIssue(TokenUtils.buildIssueMsg(nbSpace, endMessage), keyword);
      }

    }
  }

}
