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

  @Override
  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (formattingCheck.isFunctionCallsArgumentsIndentation && node.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      checkArgumentsIndentation(formattingCheck, node, PHPGrammar.PARAMETER_LIST_FOR_CALL);
    }
    if (formattingCheck.isMethodArgumentsIndentation && node.is(PHPGrammar.PARAMETER_LIST)) {
      checkArgumentsIndentation(formattingCheck, node, PHPGrammar.PARAMETER);
    }
    if (formattingCheck.isInterfacesIndentation && node.is(PHPGrammar.CLASS_DECLARATION)) {
      checkImplementListIndentation(formattingCheck, node);
    }
  }

  private void checkImplementListIndentation(FormattingStandardCheck formattingCheck, AstNode node) {
    AstNode implementList = node.getFirstChild(PHPGrammar.IMPLEMENTS_LIST);

    if (implementList != null) {
      List<AstNode> interfaceList = implementList.getFirstChild(PHPGrammar.INTERFACE_LIST).getChildren(PHPGrammar.FULLY_QUALIFIED_CLASS_NAME);
      int classDecLine = node.getTokenLine();
      int expectedColumn = getLineStartingColumn(node) + PSR2_INDENTATION;

      if (!isOnSameLine(node.getToken(), Iterables.getLast(interfaceList).getToken())) {

        if (!isCorrectlySplittedOnLines(classDecLine, interfaceList)) {
          formattingCheck.reportIssue("Either split this list into multiple lines or move it on the same line \"" + classDecLine + "\".",
            interfaceList.get(0));
        } else if (!isCorrectlyIndented(expectedColumn, interfaceList)) {
          formattingCheck.reportIssue("Align all interfaces in this list at column \"" + expectedColumn + "\".", interfaceList.get(0));
        }
      }
    }
  }

  private void checkArgumentsIndentation(FormattingStandardCheck formattingCheck, AstNode node, AstNodeType parameterNodeType) {
    List<AstNode> arguments = node.getChildren(parameterNodeType);

    if (arguments.size() > 1) {
      Token lastParam = Iterables.getLast(arguments).getLastToken();
      AstNode methodName = node.getPreviousAstNode();
      AstNode firstParam = arguments.get(0);
      int expectedIndentationColumn = getLineStartingColumn(node.getParent()) + PSR2_INDENTATION;
      int callingLine = methodName.getTokenLine();

      if (!isOnSameLine(methodName.getToken(), lastParam)) {

        if (!isCorrectlySplittedOnLines(callingLine, arguments)) {
          formattingCheck.reportIssue("Either split this list into multiple lines, aligned at column \""
            + expectedIndentationColumn + "\" or put all arguments on line \""
            + callingLine + "\".", firstParam);
        } else if (!isCorrectlyIndented(expectedIndentationColumn, arguments)) {
          formattingCheck.reportIssue("Align all arguments in this list at column \"" + expectedIndentationColumn + "\".", firstParam);
        }
        checkClosingParenthesisLocation(formattingCheck, node, lastParam);
      }
    }
  }

  private void checkClosingParenthesisLocation(FormattingStandardCheck formattingCheck, AstNode paramList, Token lastParam) {
    AstNode rParenthesis;
    String msg;

    if (paramList.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      rParenthesis = paramList.getFirstChild(PHPPunctuator.RPARENTHESIS);
      msg = "Move the closing parenthesis on the next line.";
    } else {
      rParenthesis = paramList.getParent().getFirstChild(PHPPunctuator.RPARENTHESIS);
      msg = "Move the closing parenthesis with the opening brace on the next line.";
    }
    if (!lastParam.getType().equals(PHPPunctuator.RPARENTHESIS) && isOnSameLine(lastParam, rParenthesis.getToken())) {
      formattingCheck.reportIssue(msg, rParenthesis);
    }
  }

  private int getLineStartingColumn(AstNode node) {
    int line = node.getTokenLine();
    AstNode previousNode = node.getPreviousAstNode();
    int column = node.getToken().getColumn();

    while (previousNode != null && previousNode.getToken().getLine() == line) {
      column = previousNode.getToken().getColumn();
      previousNode = previousNode.getParent();
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
      if (item.getTokenLine() < expectedLine) {
        return false;
      }
      expectedLine++;
    }
    return true;
  }

}
