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

public class ControlStructureSpacingCheck extends SpacingCheck {

  @Override
  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isOneSpaceBetweenKeywordAndNextToken && node.is(FormattingStandardCheck.getControlStructureNodes())) {
      checkSpaceBetweenKeywordAndNextNode(formattingCheck, node);
    }
    if (formattingCheck.isOneSpaceAfterForLoopSemicolon && node.is(PHPGrammar.FOR_STATEMENT)) {
      checkSpaceForStatement(formattingCheck, node);
    }
    if (formattingCheck.isSpaceForeachStatement && node.is(PHPGrammar.FOREACH_STATEMENT)) {
      checkForeachStatement(formattingCheck, node);
    }
  }

  /**
   * Check there is exactly one space around "as" keyword and "=>" punctuator in foreach statement.
   */
  private void checkForeachStatement(FormattingStandardCheck formattingCheck, AstNode node) {
    AstNode foreachExpr = node.getFirstChild(PHPGrammar.FOREACH_EXPR);
    AstNode asKeyword = foreachExpr.getFirstChild(PHPKeyword.AS);
    AstNode doubleArrow = foreachExpr.getFirstChild(PHPPunctuator.DOUBLEARROW);

    boolean isSpaceCorrectAs = isSpaceAround(asKeyword, 1 /* space before*/, 1/* space after */);
    boolean isSpaceCorrectDoubleArrow = doubleArrow == null || isSpaceAround(doubleArrow, 1/* space before*/, 1/* space after */);

    String keyword = null;
    if (!isSpaceCorrectAs && isSpaceCorrectDoubleArrow) {
      keyword = "\"" + asKeyword.getTokenOriginalValue() + "\"";
    } else if (isSpaceCorrectAs && !isSpaceCorrectDoubleArrow) {
      keyword = "\"" + doubleArrow.getTokenOriginalValue() + "\"";
    } else if (!isSpaceCorrectAs && !isSpaceCorrectDoubleArrow) {
      keyword = "\"" + asKeyword.getTokenOriginalValue() + "\" and \"" + doubleArrow.getTokenOriginalValue() + "\"";
    }

    if (keyword != null) {
      formattingCheck.reportIssue("Put exactly one space after and before " + keyword + " in \"foreach\" statement.", node);
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

    if (isType(nextToken, PHPPunctuator.LCURLYBRACE, PHPPunctuator.LPARENTHESIS) && isOnSameLine(keyword.getToken(), nextToken)) {
      int nbSpace = getNbSpaceBetween(keyword.getToken(), nextToken);

      if (nbSpace != 1) {
        String endMsg = "between this \"" + keyword.getTokenOriginalValue() + "\" keyword and the opening "
          + (isType(nextToken, PHPPunctuator.LPARENTHESIS) ? "parenthesis." : "curly brace.");
        formattingCheck.reportIssue(buildIssueMsg(nbSpace, endMsg), keyword);
      }
    }
  }

}
