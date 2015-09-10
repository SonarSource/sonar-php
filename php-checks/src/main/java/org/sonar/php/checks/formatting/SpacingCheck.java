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

import com.google.common.base.Preconditions;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import org.sonar.php.checks.FormattingStandardCheck;

public abstract class SpacingCheck {

  public abstract void visitNode(FormattingStandardCheck formattingCheck, AstNode node);

  protected String buildIssueMsg(int nbSpace, String end) {
    return (new StringBuilder()).append("Put ")
      .append(nbSpace > 1 ? "only " : "")
      .append("one space ")
      .append(end).toString();
  }

  /**
   * Return true if the given token is one of the given types.
   */
  protected boolean isType(Token token, TokenType... types) {
    boolean isOneOfType = false;
    for (TokenType type : types) {
      isOneOfType |= token.getType().equals(type);
    }
    return isOneOfType;
  }

  /**
   * Returns true if all the tokens given as parameters are on the same line.
   */
  protected boolean isOnSameLine(Token... tokens) {
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
  protected int getNbSpaceBetween(Token token1, Token token2) {
    int token1EndColumn = token1.getColumn() + (token1.getValue().length() - 1);
    int tok2StartColumn = token2.getColumn();

    return tok2StartColumn - token1EndColumn - 1;
  }

  /**
   * Check the number of space before and after the given node.
   */
  protected boolean isSpaceAround(AstNode node, int before, int after) {
    int spaceBefore = getNbSpaceBetween(node.getPreviousAstNode().getLastToken(), node.getToken());
    int spaceAfter = getNbSpaceBetween(node.getToken(), node.getNextAstNode().getToken());
    return spaceBefore == before && spaceAfter == after;
  }

}
