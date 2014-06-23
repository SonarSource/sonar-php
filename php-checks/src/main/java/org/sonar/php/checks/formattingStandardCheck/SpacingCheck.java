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
      AstNode nextNode = node.getNextAstNode();
      int expectedColumn = node.getToken().getColumn() + 2;

      if (nextNode.getToken().getType().equals(PHPPunctuator.LCURLYBRACE)) {
        Token curlyBraceToken = nextNode.getToken();

       // Do not trigger issue when prenthesis and curly brace are not on the same line
        if (curlyBraceToken.getLine() == node.getTokenLine() && curlyBraceToken.getColumn() != expectedColumn) {
          formattingCheck.reportIssue("There should be exactly one space between closing parenthesis and opening curly braces.", node);
        }
      }
    }
  }

}
