/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
package org.sonar.php.checks.formattingStandardCheck;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.parser.PHPGrammar;

public class SpacingCheck {

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isOneSpaceBetweenRParentAndLCurly && node.is(PHPPunctuator.RPARENTHESIS)) {
      checkSpaceBetweenRParentAndLCurly(formattingCheck, node);
    }
    if (formattingCheck.isOneSpaceBetweenKeywordAndNextToken && node.is(formattingCheck.CONTROL_STRUCTURE)) {
      checkSpaceBetweenKeywordAndNextNode(formattingCheck, node);
    }
    if (formattingCheck.isOneSpaceAfterForLoopSemicolon && node.is(PHPGrammar.FOR_STATEMENT)) {
      checkSpaceForStatement(formattingCheck, node);
    }
  }

  /**
   * Check there is exactly one space after each ";" in for statement.
   */
  private void checkSpaceForStatement(FormattingStandardCheck formattingCheck, AstNode node) {
    boolean shouldReportIssue = false;

    for (AstNode semicolon : node.getChildren(PHPPunctuator.SEMICOLON)) {
      int nbSpace = getNbSpaceBetween(semicolon.getToken(), semicolon.getNextAstNode().getToken());
      if (nbSpace != 1) {
        shouldReportIssue = true;
      }
    }

    if (shouldReportIssue) {
      formattingCheck.reportIssue("Put exactly one space after each \";\" character in the \"for\" statement.", node);
    }
  }

  private void checkSpaceBetweenKeywordAndNextNode(FormattingStandardCheck formattingCheck, AstNode controlStructure) {
    AstNode keyword = controlStructure.getFirstChild(PHPKeyword.values());
    Token nextToken = keyword.getNextAstNode().getToken();

    if (isOpeningParenthesisOrCurlyBrace(nextToken) && nextToken.getLine() == keyword.getTokenLine()) {

      // column of the end of the keyword
      int keywordEndColumn = keyword.getToken().getColumn() + (keyword.getTokenValue().length() - 1);
      int nbSpace = nextToken.getColumn() - keywordEndColumn - 1;

      if (nbSpace != 1) {
        String msg = (new StringBuilder())
          .append("Put ")
          .append((nbSpace > 1 ? "only " : ""))
          .append("one space between this \"")
          .append(keyword.getTokenOriginalValue())
          .append("\" keyword and the opening ")
          .append((nextToken.getType().equals(PHPPunctuator.LPARENTHESIS) ? "parenthesis." : "curly brace.")).toString();

        formattingCheck.reportIssue(msg, keyword);
      }
    }
  }

  private void checkSpaceBetweenRParentAndLCurly(FormattingStandardCheck formattingCheck, AstNode rParenthesis) {
    Token nextToken = rParenthesis.getNextAstNode().getToken();

    if (nextToken.getType().equals(PHPPunctuator.LCURLYBRACE)) {
      int nbSpace = getNbSpaceBetween(rParenthesis.getToken(), nextToken);

      if (nbSpace != 1) {
        String msg = (new StringBuilder())
          .append("Put ")
          .append((nbSpace > 1 ? "only " : ""))
          .append("one space between the closing parenthesis and the opening curly brace.").toString();

        formattingCheck.reportIssue(msg, rParenthesis);
      }
    }
  }

  private boolean isOpeningParenthesisOrCurlyBrace(Token token) {
    return token.getType().equals(PHPPunctuator.LPARENTHESIS) || token.getType().equals(PHPPunctuator.LCURLYBRACE);
  }

  /**
   * Returns number of space between the 2 tokens, 1 if there are on different line.
   */
  private int getNbSpaceBetween(Token token1, Token token2) {
    if (token1.getLine() != token2.getLine()) {
      return 1;
    }
    return token2.getColumn() - token1.getColumn() - 1;
  }
}
