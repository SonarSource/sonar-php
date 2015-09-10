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

import javax.annotation.Nullable;

public class CurlyBraceCheck {

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (node.is(FormattingStandardCheck.getClassAndFunctionNodes()) && formattingCheck.isOpenCurlyBraceForClassAndFunction) {
      checkLCurlyForClassAndFunction(formattingCheck, getLeftCurlyBraceNode(node));
    }
    if (node.is(FormattingStandardCheck.getControlStructureNodes()) && formattingCheck.isOpenCurlyBraceForControlStructures) {
      checkLCurlyForControlStructure(formattingCheck, getLeftCurlyBraceNode(node));
    }
    if (node.is(PHPGrammar.ELSE_CLAUSE, PHPGrammar.CATCH_STATEMENT, PHPGrammar.FINALLY_STATEMENT) && formattingCheck.isClosingCurlyNextToKeyword) {
      checkRCurlyBraceOnSameLine(formattingCheck, node);
    }
  }

  /**
   * Check that else, catch or finally keywords are on the same line as the previous closing curly brace.
   */
  private void checkRCurlyBraceOnSameLine(FormattingStandardCheck formattingCheck, AstNode node) {
    Token previsouToken = node.getPreviousAstNode().getLastToken();
    String keyword = node.getFirstChild(PHPKeyword.ELSE, PHPKeyword.CATCH, PHPKeyword.FINALLY).getTokenOriginalValue();

    if (previsouToken.getType().equals(PHPPunctuator.RCURLYBRACE) && previsouToken.getLine() != node.getTokenLine()) {
      formattingCheck.reportIssue("Move this \"" + keyword + "\" to the same line as the previous closing curly brace.", node);
    }
  }

  /**
   * Check that control structure opening curly brace ends line.
   */
  private void checkLCurlyForControlStructure(FormattingStandardCheck formattingCheck, @Nullable AstNode leftCurlyBrace) {
    if (leftCurlyBrace != null && !endsLine(leftCurlyBrace)) {
      formattingCheck.reportIssue("Move this open curly brace to the end of the previous line.", leftCurlyBrace);
    }
  }

  /**
   * Check that class and function opening curly brace starts line.
   */
  private void checkLCurlyForClassAndFunction(FormattingStandardCheck formattingCheck, @Nullable AstNode leftCurlyBrace) {
    if (leftCurlyBrace != null && !isFirstOnline(leftCurlyBrace)) {
      formattingCheck.reportIssue("Move this open curly brace to the beginning of the next line.", leftCurlyBrace);
    }
  }

  private boolean isFirstOnline(AstNode curlyBrace) {
    Token previousToken = curlyBrace.getPreviousAstNode().getLastToken();

    // In one case, clonsing parenthesis can be on the same line as the opening curly brace
    if (previousToken.getType().equals(PHPPunctuator.RPARENTHESIS)) {
      previousToken = curlyBrace.getPreviousAstNode().getPreviousAstNode().getLastToken();
    }

    return previousToken.getLine() != curlyBrace.getTokenLine();
  }

  private boolean endsLine(AstNode curlyBrace) {
    return curlyBrace.getPreviousAstNode().getLastToken().getLine() == curlyBrace.getTokenLine();
  }

  @Nullable
  private AstNode getLeftCurlyBraceNode(AstNode node) {
    AstNode lcurlyBrace = null;
    AstNode child = node.getFirstChild(PHPPunctuator.LCURLYBRACE, PHPGrammar.BLOCK, PHPGrammar.STATEMENT, PHPGrammar.METHOD_BODY, PHPGrammar.SWITCH_CASE_LIST);

    if (child == null) {
      // do nothing
    } else if (child.is(PHPGrammar.BLOCK, PHPGrammar.SWITCH_CASE_LIST)) {
      lcurlyBrace = child.getFirstChild(PHPPunctuator.LCURLYBRACE);

    } else if (child.is(PHPGrammar.STATEMENT)) {
      AstNode stmtChild = child.getFirstChild();
      if (stmtChild.is(PHPGrammar.BLOCK)) {
        lcurlyBrace = stmtChild.getFirstChild(PHPPunctuator.LCURLYBRACE);
      }

    } else if (child.is(PHPGrammar.METHOD_BODY)) {
      AstNode body = child.getFirstChild(PHPGrammar.BLOCK);
      if (body != null) {
        lcurlyBrace = body.getFirstChild(PHPPunctuator.LCURLYBRACE);
      }
    } else {
      lcurlyBrace = child;
    }

    return lcurlyBrace;
  }
}
