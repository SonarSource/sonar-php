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

import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.checks.utils.TokenVisitor;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.Nullable;
import java.util.List;

public class FunctionSpacingCheck extends PHPVisitorCheck implements FormattingCheck {

  private static final String METHOD_NAME_SPACES_MESSAGE = "Remove all space between the method name \"%s\" and the opening parenthesis.";
  private static final String CLOSURE_SPACES_MESSAGE = "Put exactly one space between the \"function\" keyword and the opening parenthesis.";
  private static final String CLOSURE_LEXICAL_VARS_SPACES_MESSAGE = "Put exactly one space before and after the \"use\" keyword.";
  private static final String[] COMMA_SPACES_MESSAGE = {
    "Remove any space before comma separated arguments.",
    "Put exactly one space after comma separated arguments.",
    "Remove any space before comma separated arguments and put exactly one space after comma separated arguments."
  };

  private FormattingStandardCheck check;

  @Override
  public void checkFormat(FormattingStandardCheck formattingCheck, ScriptTree scriptTree) {
    this.check = formattingCheck;
    super.visitScript(scriptTree);
  }

  @Override
  public void visitParameterList(ParameterListTree tree) {
    checkSpaceForComma(new TokenVisitor(tree), tree.parameters().getSeparators(), tree);
    super.visitParameterList(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (!check.isInternalFunction(tree.callee())) {
      checkSpaceForComma(new TokenVisitor(tree), tree.arguments().getSeparators(), tree.openParenthesisToken());
      checkSpaceAfterFunctionName(((PHPTree) tree.callee()).getLastToken(), tree.openParenthesisToken());
    }

    super.visitFunctionCall(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    checkSpaceAfterFunctionName(tree.name().token(), tree.parameters().openParenthesisToken());
    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkSpaceAfterFunctionName(tree.name().token(), tree.parameters().openParenthesisToken());
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkClosureSpacing(tree.functionToken(), tree.parameters(), tree.lexicalVars());
    super.visitFunctionExpression(tree);
  }

  private void checkClosureSpacing(SyntaxToken functionKeyword, ParameterListTree parameters, @Nullable LexicalVariablesTree lexicalVars) {
    if (check.isClosureSpacing) {

      if (TokenUtils.getNbSpaceBetween(functionKeyword, parameters.openParenthesisToken()) != 1) {
        check.reportIssue(CLOSURE_SPACES_MESSAGE, functionKeyword);
      }

      if (lexicalVars != null) {
        int spaceBeforeUse = TokenUtils.getNbSpaceBetween(parameters.closeParenthesisToken(), lexicalVars.useToken());
        int spaceAfterUse = TokenUtils.getNbSpaceBetween(lexicalVars.useToken(), lexicalVars.openParenthesisToken());

        if (spaceBeforeUse != 1 || spaceAfterUse != 1) {
          check.reportIssue(CLOSURE_LEXICAL_VARS_SPACES_MESSAGE, lexicalVars);
        }
      }
    }
  }

  /**
   * Check there is not space between a function's name and the opening parenthesis.
   */
  private void checkSpaceAfterFunctionName(SyntaxToken functionName, SyntaxToken openParenthesis) {
    if (check.isNoSpaceAfterMethodName && TokenUtils.getNbSpaceBetween(functionName, openParenthesis) != 0) {
      check.reportIssue(String.format(METHOD_NAME_SPACES_MESSAGE, functionName.text()), functionName);
    }
  }

  /**
   * Check space around the arguments' comma.
   */
  private void checkSpaceForComma(TokenVisitor tokenVisitor, List<SyntaxToken> commas, Tree tree) {
    if (check.isOneSpaceAfterComma) {

    int msgIndex = -1;

      for (SyntaxToken commaToken : commas) {
        SyntaxToken nextToken = tokenVisitor.nextToken(commaToken);
        SyntaxToken previousToken = tokenVisitor.prevToken(commaToken);

        if (TokenUtils.isOnSameLine(previousToken, commaToken, nextToken)) {
          boolean isSpaceBeforeOK = TokenUtils.getNbSpaceBetween(previousToken, commaToken) == 0;
          boolean isSpaceAfterOK = TokenUtils.getNbSpaceBetween(commaToken, nextToken) == 1;

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
        check.reportIssue(COMMA_SPACES_MESSAGE[msgIndex], tree);
      }

    }
  }

}
