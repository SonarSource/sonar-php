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

import com.google.common.base.Preconditions;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
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
    if (formattingCheck.isOneSpaceAfterComma && node.is(PHPGrammar.PARAMETER_LIST, PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      checkSpaceForComma(formattingCheck, node);
    }
    if (formattingCheck.isNoSpaceAfterMethodName && node.is(PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      checkSpaceAfterFunctionName(formattingCheck, node);
    }
  }

  /**
   * Check there is not space between a function's name and the opening parenthesis.
   */
  private void checkSpaceAfterFunctionName(FormattingStandardCheck formattingCheck, AstNode node) {
    Token lParenToken = node.getFirstChild(PHPPunctuator.LPARENTHESIS).getToken();
    Token funcNameToken = node.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST) ?
      node.getPreviousAstNode().getLastToken() : node.getFirstChild(GenericTokenType.IDENTIFIER).getToken();

    int nbSpace = lParenToken.getColumn() - (funcNameToken.getColumn() + funcNameToken.getValue().length());
    if (nbSpace != 0) {
      formattingCheck.reportIssue("Remove all space between the method name \"" + funcNameToken.getOriginalValue() + "\" and the opening parenthesis.", node);
    }
  }

  /**
   * Check space around the arguments'
   */
  private void checkSpaceForComma(FormattingStandardCheck formattingCheck, AstNode node) {
    int msgIndex = -1;
    String[] msg = {
      "Remove any space before comma separated arguments.",
      "Put exactly one space after comma separated arguments.",
      "Remove any space before comma separated arguments and put exactly one space after comma separated arguments."
    };
    for (AstNode comma : node.getChildren(PHPPunctuator.COMMA)) {
      Token commaToken = comma.getToken();
      Token nextToken = comma.getNextSibling().getToken();
      Token previousToken = comma.getPreviousSibling().getLastToken();

      if (isOnSameLine(previousToken, commaToken, nextToken)) {
        boolean isSpaceBeforeOK = commaToken.getColumn() - (previousToken.getColumn() + previousToken.getValue().length()) == 0;
        boolean isSpaceAfterOK = getNbSpaceBetween(commaToken, nextToken) == 1;

        if (!isSpaceBeforeOK && isSpaceAfterOK && msgIndex < 0) {
          msgIndex = 0;
        } else if (isSpaceBeforeOK && !isSpaceAfterOK && msgIndex < 0) {
          msgIndex = 1;
        } else if (!isSpaceBeforeOK && !isSpaceAfterOK) {
          msgIndex = 2;
          break;
        }
      }
    }
    if (msgIndex > -1) {
      formattingCheck.reportIssue(msg[msgIndex], node);
    }
  }

  /**
   * Check there is exactly one space after each ";" in for statement.
   */
  private void checkSpaceForStatement(FormattingStandardCheck formattingCheck, AstNode node) {
    boolean shouldReportIssue = false;

    for (AstNode semicolon : node.getChildren(PHPPunctuator.SEMICOLON)) {
      Token semicolonToken = semicolon.getToken();
      Token nextToken = semicolon.getNextAstNode().getToken();
      int nbSpace = getNbSpaceBetween(semicolonToken, nextToken);

      if (nbSpace != 1 && isOnSameLine(semicolonToken, nextToken)) {
        shouldReportIssue = true;
      }
    }

    if (shouldReportIssue) {
      formattingCheck.reportIssue("Put exactly one space after each \";\" character in the \"for\" statement.", node);
    }
  }

  /**
   * Check that there is exactly one space between a control structure keyword and a opening parenthesis or curly brace.
   */
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

  /**
   * Check that there is exactly one space between a closing parenthesis and a opening curly brace.
   */
  private void checkSpaceBetweenRParentAndLCurly(FormattingStandardCheck formattingCheck, AstNode rParenthesis) {
    Token nextToken = rParenthesis.getNextAstNode().getToken();
    Token rParenToken = rParenthesis.getToken();

    if (nextToken.getType().equals(PHPPunctuator.LCURLYBRACE)) {
      int nbSpace = getNbSpaceBetween(rParenToken, nextToken);

      if (nbSpace != 1 && isOnSameLine(rParenToken, nextToken)) {
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
   * Returns true if all the tokens given as parameters are on the same line.
   */
  private boolean isOnSameLine(Token... tokens) {
    Preconditions.checkArgument(tokens.length > 0);

    int lineRef = tokens[0].getLine();
    for (Token token : tokens) {
      if (token.getLine() != lineRef) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns number of space between the 2 tokens.
   */
  private int getNbSpaceBetween(Token token1, Token token2) {
    return token2.getColumn() - token1.getColumn() - 1;
  }
}
