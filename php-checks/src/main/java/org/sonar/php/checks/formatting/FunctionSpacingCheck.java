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
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.parser.PHPGrammar;

public class FunctionSpacingCheck extends SpacingCheck {

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isOneSpaceAfterComma && node.is(PHPGrammar.PARAMETER_LIST, PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      checkSpaceForComma(formattingCheck, node);
    }
    if (formattingCheck.isNoSpaceAfterMethodName && node.is(PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      checkSpaceAfterFunctionName(formattingCheck, node);
    }
    if (formattingCheck.isClosureSpacing && node.is(PHPGrammar.FUNCTION_EXPRESSION)) {
      checkClosureSpacing(formattingCheck, node);
    }
  }

  private void checkClosureSpacing(FormattingStandardCheck formattingCheck, AstNode node) {
    Token lParenToken = node.getFirstChild(PHPPunctuator.LPARENTHESIS).getToken();
    Token functionKeyword = node.getFirstChild(PHPKeyword.FUNCTION).getToken();

    if (getNbSpaceBetween(functionKeyword, lParenToken) != 1) {
      formattingCheck.reportIssue("Put exactly one space between the \"function\" keyword and the opening parenthesis.", node);
    }

    AstNode lexicalVars = node.getFirstChild(PHPGrammar.LEXICAL_VARS);
    if (lexicalVars != null && !isSpaceAround(lexicalVars.getFirstChild(PHPKeyword.USE), 1 /* space before */, 1 /* space after */)) {
      formattingCheck.reportIssue("Put exactly one space before and after the \"use\" keyword.", lexicalVars);
    }
  }

  /**
   * Check there is not space between a function's name and the opening parenthesis.
   */
  private void checkSpaceAfterFunctionName(FormattingStandardCheck formattingCheck, AstNode node) {
    Token lParenToken = node.getFirstChild(PHPPunctuator.LPARENTHESIS).getToken();
    Token funcNameToken = node.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST) ?
      node.getPreviousAstNode().getLastToken() : node.getFirstChild(PHPGrammar.IDENTIFIER).getToken();

    if (getNbSpaceBetween(funcNameToken, lParenToken) != 0) {
      formattingCheck.reportIssue("Remove all space between the method name \"" + funcNameToken.getOriginalValue() + "\" and the opening parenthesis.", node);
    }
  }

  /**
   * Check space around the arguments' comma.
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
        boolean isSpaceBeforeOK = getNbSpaceBetween(previousToken, commaToken) == 0;
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

}
