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
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;

public class SpacingCheck {

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isOneSpaceBetweenRParentAndLCurly && node.is(PHPPunctuator.RPARENTHESIS)) {
      checkSpaceBetweenRParentAndLCurly(formattingCheck, node);
    }
  }

  private void checkSpaceBetweenRParentAndLCurly(FormattingStandardCheck formattingCheck, AstNode rParenthesis) {
    Token nextToken = rParenthesis.getNextAstNode().getToken();

    if (nextToken.getType().equals(PHPPunctuator.LCURLYBRACE)) {

      // Do not trigger issue when prenthesis and curly brace are not on the same line
      if (hasNotExactlyOneSpaceBetween(rParenthesis.getToken(), nextToken)) {
        formattingCheck.reportIssue("There should be exactly one space between closing parenthesis and opening curly braces.", rParenthesis);
      }
    }
  }

  /**
   * Returns true if both tokens are on the same line and there is not exactly one space between them.
   */
  private boolean hasNotExactlyOneSpaceBetween(Token token1, Token token2) {
    int expectedColumn = token1.getColumn() + 2;
    return token1.getLine() == token2.getLine() && token2.getColumn() != expectedColumn;
  }
}
