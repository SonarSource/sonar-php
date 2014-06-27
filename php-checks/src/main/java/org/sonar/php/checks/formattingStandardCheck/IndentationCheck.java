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
package org.sonar.php.checks.formattingstandardcheck;

import com.google.common.collect.Iterables;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Token;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.parser.PHPGrammar;

import java.util.List;

public class IndentationCheck extends SpacingCheck {

  private static final int PSR2_INDENTATION = 4;

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isMethodArgumentsIndentation && node.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      checkArgumentsIndentation(formattingCheck, node, PHPGrammar.PARAMETER_LIST_FOR_CALL);
    }
  }

  private void checkArgumentsIndentation(FormattingStandardCheck formattingCheck, AstNode node, AstNodeType parameterNodeType) {
    List<AstNode> arguments = node.getChildren(parameterNodeType);

    if (arguments.size() > 1) {
      Token last = Iterables.getLast(arguments).getLastToken();
      AstNode caller = node.getPreviousAstNode();
      AstNode first = arguments.get(0);
      int expectedIndentationColumn = getLineStartingColumn(node.getParent()) + PSR2_INDENTATION;
      int callingLine = caller.getTokenLine();
      AstNode rParenthesis = node.getFirstChild(PHPPunctuator.RPARENTHESIS);

      if (!isOnSameLine(caller.getToken(), last)) {
        if (!isCorrectlySplittedOnLines(callingLine, arguments)) {
          formattingCheck.reportIssue("Either split this list into multiple lines and aligned at column \"" + expectedIndentationColumn + "\" or move it on the same line \"" + callingLine + "\".", first);
        } else if (!isCorrectlyIndented(expectedIndentationColumn, arguments)) {
          formattingCheck.reportIssue("Align all arguments in this list at column \"" + expectedIndentationColumn + "\".", first);
        }
        if (!last.getType().equals(PHPPunctuator.RPARENTHESIS) && isOnSameLine(last, rParenthesis.getToken())) {
          if (node.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
            formattingCheck.reportIssue("Move the closing parenthesis on the next line.", rParenthesis);
          }
        }
      }
    }
  }

  private int getLineStartingColumn(AstNode node) {
    int line = node.getTokenLine();
    AstNode parentNode = node;
    int column = parentNode.getToken().getColumn();

    while (parentNode != null && parentNode.getTokenLine() == line) {
      column = parentNode.getToken().getColumn();
      parentNode = parentNode.getParent();
    }
    return column;
  }

  private boolean isCorrectlyIndented(int expectedColumn, List<AstNode> items) {
    for (AstNode item : items) {
      if (item.getToken().getColumn() != expectedColumn) {
        return false;
      }
    }
    return true;
  }

  private boolean isCorrectlySplittedOnLines(int referenceLine, List<AstNode> items) {
    int expectedLine = referenceLine + 1;
    for (AstNode item : items) {
      if (item.getTokenLine() != expectedLine) {
        return false;
      }
      expectedLine++;
    }
    return true;
  }

}
