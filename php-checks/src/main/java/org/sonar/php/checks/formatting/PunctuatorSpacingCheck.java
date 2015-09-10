/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks.formatting;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;

public class PunctuatorSpacingCheck extends SpacingCheck {

  @Override
  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isOneSpaceBetweenRParentAndLCurly && node.is(PHPPunctuator.RPARENTHESIS)) {
      checkSpaceBetweenRParentAndLCurly(formattingCheck, node);
    }
    if (formattingCheck.isNoSpaceParenthesis && node.is(PHPPunctuator.RPARENTHESIS)) {
      checkSpaceInsideParenthesis(formattingCheck, node);
    }
  }

  /**
   * Check there is no space after the opening parenthesis and no space before the closing one.
   */
  private void checkSpaceInsideParenthesis(FormattingStandardCheck formattingCheck, AstNode rcurly) {
    AstNode lcurly = rcurly.getParent().getFirstChild(PHPPunctuator.LPARENTHESIS);
    Token lcurlyNextToken = lcurly.getNextAstNode().getToken();
    Token rculyPreviousToken = rcurly.getPreviousAstNode().getLastToken();

    boolean isLCurlyOK = !isOnSameLine(lcurlyNextToken, lcurly.getToken()) || getNbSpaceBetween(lcurly.getToken(), lcurlyNextToken) == 0;
    boolean isRCurlyOK = !isOnSameLine(rculyPreviousToken, rcurly.getToken()) || getNbSpaceBetween(rculyPreviousToken, rcurly.getToken()) == 0;

    if (!isLCurlyOK && isRCurlyOK) {
      formattingCheck.reportIssue("Remove all space after the opening parenthesis.", lcurly);
    } else if (isLCurlyOK && !isRCurlyOK) {
      formattingCheck.reportIssue("Remove all space before the closing parenthesis.", rcurly);
    } else if (!isLCurlyOK && !isRCurlyOK) {
      formattingCheck.reportIssue("Remove all space after the opening parenthesis and before the closing parenthesis.", lcurly);
    }
  }

  /**
   * Check that there is exactly one space between a closing parenthesis and a opening curly brace.
   */
  private void checkSpaceBetweenRParentAndLCurly(FormattingStandardCheck formattingCheck, AstNode rParenthesis) {
    Token nextToken = rParenthesis.getNextAstNode().getToken();
    Token rParenToken = rParenthesis.getToken();

    if (isType(nextToken, PHPPunctuator.LCURLYBRACE)) {
      int nbSpace = getNbSpaceBetween(rParenToken, nextToken);

      if (nbSpace != 1 && isOnSameLine(rParenToken, nextToken)) {
        formattingCheck.reportIssue(buildIssueMsg(nbSpace, "between the closing parenthesis and the opening curly brace."), rParenthesis);
      }
    }
  }

}
