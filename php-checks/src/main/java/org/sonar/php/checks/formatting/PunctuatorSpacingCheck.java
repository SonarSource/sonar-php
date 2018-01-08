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

import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.ArrayDeque;
import java.util.Deque;

public class PunctuatorSpacingCheck extends PHPVisitorCheck implements FormattingCheck {

  private static final String CLOSE_PARENTHESIS_OPEN_CURLY_MESSAGE = "between the closing parenthesis and the opening curly brace.";
  private static final String OPEN_PARENTHESIS_SPACES_MESSAGE = "Remove all space after the opening parenthesis.";
  private static final String CLOSE_PARENTHESIS_SPACES_MESSAGE = "Remove all space before the closing parenthesis.";
  private static final String BOTH_PARENTHESIS_SPACES_MESSAGE = "Remove all space after the opening parenthesis and before the closing parenthesis.";

  private FormattingStandardCheck check;
  private SyntaxToken previousToken = null;
  private Deque<OpenParenthesisContext> openParenthesisLevel = new ArrayDeque<>();

  private static class OpenParenthesisContext {
    final SyntaxToken openParenthesis;
    final SyntaxToken nextToken;

    public OpenParenthesisContext(SyntaxToken openParenthesis, SyntaxToken nextToken) {
      this.openParenthesis = openParenthesis;
      this.nextToken = nextToken;
    }
  }

  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;
    this.previousToken = null;
    openParenthesisLevel.clear();
    super.visitScript(scriptTree);
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    // do not process string characters
  }

  @Override
  public void visitToken(SyntaxToken token) {
    super.visitToken(token);
    if (previousToken != null) {

      if (isCloseParenthesis(previousToken) && isOpenCurly(token)) {
        checkSpaceBetweenCloseParenAndOpenCurly(previousToken, token);
      }

      if (isOpenParenthesis(previousToken)) {
        openParenthesisLevel.push(new OpenParenthesisContext(previousToken, token));
      }

      if (isCloseParenthesis(token)) {
        checkSpaceInsideParenthesis(openParenthesisLevel.pop(), token, previousToken);
      }
    }

    previousToken = token;
  }


  private void checkSpaceBetweenCloseParenAndOpenCurly(SyntaxToken closeParenthesis, SyntaxToken openCurly) {
    if (check.isOneSpaceBetweenRParentAndLCurly) {
      int nbSpace = TokenUtils.getNbSpaceBetween(closeParenthesis, openCurly);

      if (TokenUtils.isOnSameLine(closeParenthesis, openCurly) && nbSpace != 1) {
        check.reportIssue(TokenUtils.buildIssueMsg(nbSpace, CLOSE_PARENTHESIS_OPEN_CURLY_MESSAGE), closeParenthesis);
      }
    }
  }

  /**
   * Check there is no space after the opening parenthesis and no space before the closing one.
   */
  private void checkSpaceInsideParenthesis(OpenParenthesisContext openParenthesisContext, SyntaxToken closeParen, SyntaxToken closeParenPreviousToken) {
    if (check.isNoSpaceParenthesis) {

      SyntaxToken openParen = openParenthesisContext.openParenthesis;
      SyntaxToken openParenNextToken = openParenthesisContext.nextToken;

      boolean isLCurlyOK = !TokenUtils.isOnSameLine(openParenNextToken, openParen) || TokenUtils.getNbSpaceBetween(openParen, openParenNextToken) == 0;
      boolean isRCurlyOK = !TokenUtils.isOnSameLine(closeParenPreviousToken, closeParen) || TokenUtils.getNbSpaceBetween(closeParenPreviousToken, closeParen) == 0;

      if (!isLCurlyOK && isRCurlyOK) {
        check.reportIssue(OPEN_PARENTHESIS_SPACES_MESSAGE, openParen);

      } else if (isLCurlyOK && !isRCurlyOK) {
        check.reportIssue(CLOSE_PARENTHESIS_SPACES_MESSAGE, closeParen);

      } else if (!isLCurlyOK) {
        check.reportIssue(BOTH_PARENTHESIS_SPACES_MESSAGE, openParen, closeParen);
      }
    }
  }

  private static boolean isOpenParenthesis(SyntaxToken token) {
    return TokenUtils.isType(token, PHPPunctuator.LPARENTHESIS);
  }

  private static boolean isOpenCurly(SyntaxToken token) {
    return TokenUtils.isType(token, PHPPunctuator.LCURLYBRACE);
  }

  private static boolean isCloseParenthesis(SyntaxToken token) {
    return TokenUtils.isType(token, PHPPunctuator.RPARENTHESIS);
  }

}
