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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndentationCheck extends PHPVisitorCheck implements FormattingCheck {

  private static final String ARGUMENT_LINE_SPLIT_MESSAGE =
    "Either split this list into multiple lines, aligned at column \"%s\" or put all arguments on line \"%s\".";
  private static final String ARGUMENT_INDENTATION_MESSAGE = "Align all arguments in this list at column \"%s\".";
  private static final int PSR2_INDENTATION = 4;
  private static final String FUNCTION_CALL_PARENTHESIS_MESSAGE = "Move the closing parenthesis on the next line.";
  private static final String FUNCTION_DEC_PARENTHESIS_MESSAGE = "Move the closing parenthesis with the opening brace on the next line.";
  private static final String INTERFACE_SPLIT_MESSAGE = "Either split this list into multiple lines or move it on the same line \"%s\".";
  private static final String INTERFACE_INDENTATION = "Align all interfaces in this list at column \"%s\".";

  private FormattingStandardCheck check;
  private Map<Integer, Integer> startColumnByLine = new HashMap<>();

  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;
    this.startColumnByLine.clear();

    super.visitScript(scriptTree);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (startColumnByLine.get(token.line()) == null) {
      startColumnByLine.put(token.line(), token.column());
    }

    super.visitToken(token);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    if (check.isFunctionCallsArgumentsIndentation && !check.isInternalFunction(tree.callee())) {
      SyntaxToken calleeLastToken = ((PHPTree) tree.callee()).getLastToken();

      checkArgumentsIndentation(
        tree.arguments(),
        calleeLastToken,
        startColumnForLine(calleeLastToken.line()),
        tree.closeParenthesisToken(), true);
    }
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    super.visitFunctionDeclaration(tree);

    if (check.isMethodArgumentsIndentation) {
      checkArgumentsIndentation(
        tree.parameters().parameters(),
        tree.name().token(),
        startColumnForLine(tree.functionToken().line()),
        tree.parameters().closeParenthesisToken(), false);
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    super.visitMethodDeclaration(tree);

    if (check.isMethodArgumentsIndentation) {
      checkArgumentsIndentation(
        tree.parameters().parameters(),
        tree.name().token(),
        startColumnForLine(tree.functionToken().line()),
        (tree.parameters().closeParenthesisToken()), false);
    }
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    super.visitFunctionExpression(tree);

    if (check.isMethodArgumentsIndentation) {
      checkArgumentsIndentation(
        tree.parameters().parameters(),
        tree.functionToken(),
        startColumnForLine(tree.functionToken().line()),
        (tree.parameters().closeParenthesisToken()), false);
    }
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    checkImplementListIndentation(tree);
  }

  private void checkImplementListIndentation(ClassDeclarationTree classTree) {
    if (check.isInterfacesIndentation && classTree.is(Tree.Kind.CLASS_DECLARATION) && !classTree.superInterfaces().isEmpty()) {

      List<Tree> interfaceList = ImmutableList.<Tree>copyOf(classTree.superInterfaces());
      SyntaxToken classToken = classTree.classEntryTypeToken();
      SyntaxToken lastInterfaceToken = ((PHPTree) Iterables.getLast(classTree.superInterfaces())).getFirstToken();
      int expectedColumn = classToken.column() + PSR2_INDENTATION;

      if (!TokenUtils.isOnSameLine(classToken, lastInterfaceToken)) {

        if (!isCorrectlySplitOnLines(classToken.line(), interfaceList)) {
          check.reportIssue(String.format(INTERFACE_SPLIT_MESSAGE, classToken.line()), classTree.superInterfaces().get(0));

        } else if (!isCorrectlyIndented(expectedColumn, interfaceList)) {
          check.reportIssue(String.format(INTERFACE_INDENTATION, expectedColumn), classTree.superInterfaces().get(0));
        }
      }
    }
  }

  private void checkArgumentsIndentation(SeparatedList arguments, SyntaxToken functionName, int baseColumn, @Nullable SyntaxToken closeParenthesis, boolean isFunctionCall) {
    if (arguments.size() > 1) {
      SyntaxToken lastArg = ((PHPTree) Iterables.getLast(arguments)).getLastToken();
      Tree firstArg = (Tree) arguments.get(0);

      int expectedIndentationColumn = baseColumn + PSR2_INDENTATION;
      int callingLine = functionName.line();

      if (!TokenUtils.isOnSameLine(functionName, lastArg)) {

        if (!isCorrectlySplitOnLines(callingLine, arguments)) {
          check.reportIssue(String.format(ARGUMENT_LINE_SPLIT_MESSAGE, expectedIndentationColumn, callingLine), firstArg);

        } else if (!isCorrectlyIndented(expectedIndentationColumn, arguments)) {
          check.reportIssue(String.format(ARGUMENT_INDENTATION_MESSAGE, expectedIndentationColumn), firstArg);
        }

        // Checking parenthesis
        if (closeParenthesis != null) {
          checkClosingParenthesisLocation((Tree) Iterables.getLast(arguments), closeParenthesis, isFunctionCall);
        }
      }
    }
  }

  private void checkClosingParenthesisLocation(Tree lastArgument, SyntaxToken closeParenthesis, boolean isFunctionCall) {
    if (!((PHPTree) lastArgument).getLastToken().text().equals(PHPPunctuator.RPARENTHESIS.getValue()) && TokenUtils.isOnSameLine(((PHPTree) lastArgument).getLastToken(), closeParenthesis)) {
      check.reportIssue(
        isFunctionCall ? FUNCTION_CALL_PARENTHESIS_MESSAGE : FUNCTION_DEC_PARENTHESIS_MESSAGE,
        closeParenthesis);
    }
  }

  private boolean isCorrectlyIndented(int expectedColumn, List<Tree> items) {
    for (Tree item : items) {
      if (((PHPTree) item).getFirstToken().column() != expectedColumn) {
        return false;
      }
    }
    return true;
  }

  private boolean isCorrectlySplitOnLines(int referenceLine, List<Tree> items) {
    int expectedLine = referenceLine + 1;

    for (Tree item : items) {
      if (((PHPTree) item).getFirstToken().line() < expectedLine) {
        return false;
      }
      expectedLine++;
    }
    return true;
  }

  private int startColumnForLine(int line) {
    return startColumnByLine.get(line);
  }

}
