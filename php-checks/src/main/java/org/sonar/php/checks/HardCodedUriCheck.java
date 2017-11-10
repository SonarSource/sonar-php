/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.tree.Tree.Kind.REGULAR_STRING_LITERAL;

@Rule(key = "S1075")
public class HardCodedUriCheck extends PHPVisitorCheck {
  private static final String SCHEME = "[a-zA-Z][a-zA-Z\\+\\.\\-]+";
  private static final String FOLDER_NAME = "[^/?%*:\\\\|\"<>]+";
  private static final String URI_REGEX = String.format("^%s://.+", SCHEME);
  private static final String LOCAL_URI = String.format("^(~/|/|//[\\w-]+/|%s:/)(%s/)*%s/?", SCHEME, FOLDER_NAME, FOLDER_NAME);
  private static final String BACKSLASH_LOCAL_URI = String.format("^(~\\\\\\\\|\\\\\\\\\\\\\\\\[\\w-]+\\\\\\\\|%s:\\\\\\\\)(%s\\\\\\\\)*%s(\\\\\\\\)?",
    SCHEME, FOLDER_NAME, FOLDER_NAME);
  private static final String DISK_URI = "^[A-Za-z]:(/|\\\\)";

  private static final Pattern URI_PATTERN = Pattern.compile(URI_REGEX + "|" + LOCAL_URI + "|" + DISK_URI + "|" + BACKSLASH_LOCAL_URI);
  private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("filename|path", Pattern.CASE_INSENSITIVE);
  private static final Pattern PATH_DELIMETERS_PATTERN = Pattern.compile("\"/\"|\"//\"|\"\\\\\\\\\"|\"\\\\\\\\\\\\\\\\\"");


  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getFunctionName(tree);
    if (functionName != null && (functionName.startsWith("preg_") || functionName.equals("define"))) {
      return;
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    String value = trimQuotes(tree.value());
    if (value.length() > 2 && tree.is(REGULAR_STRING_LITERAL) && URI_PATTERN.matcher(value).find()) {
      reportHardcodedURI(tree);
    }
    super.visitLiteral(tree);
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    if(URI_PATTERN.matcher(trimQuotes(tree.value())).find()) {
      reportHardcodedURI(tree);
    }
    super.visitExpandableStringCharacters(tree);
  }


  @Override
  public void visitVariableDeclaration(VariableDeclarationTree tree) {
    if(isFileNameVariable(tree.identifier())) {
      checkExpression(tree.initValue());
    }
    super.visitVariableDeclaration(tree);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if(tree.variable().is(Tree.Kind.VARIABLE_IDENTIFIER) && isFileNameVariable(((VariableIdentifierTree) tree.variable()).variableExpression())) {
      checkExpression(tree.value());
    }
    super.visitAssignmentExpression(tree);
  }

  private static boolean isFileNameVariable(@Nullable IdentifierTree variable) {
    return variable != null && VARIABLE_NAME_PATTERN.matcher(variable.text()).find();
  }

  private void checkExpression(@Nullable ExpressionTree expr) {
    if (expr != null) {
      if (isHardcodedURI(expr)) {
        reportHardcodedURI(expr);
      } else {
        reportStringConcatenationWithPathDelimiter(expr);
      }
    }
  }

  private static boolean isHardcodedURI(ExpressionTree expr) {
    ExpressionTree newExpr = CheckUtils.skipParenthesis(expr);
    if (!newExpr.is(REGULAR_STRING_LITERAL)) {
      return false;
    }
    String stringLiteral = trimQuotes(((LiteralTree) newExpr).value());
    return stringLiteral.length() > 2 && URI_PATTERN.matcher(stringLiteral).find();
  }

  private static String trimQuotes(String value) {
    return value.substring(1, value.length());
  }

  private void reportHardcodedURI(ExpressionTree hardcodedURI) {
    context().newIssue(this, hardcodedURI, "Refactor your code to get this URI from a customizable parameter.");
  }

  private void reportStringConcatenationWithPathDelimiter(ExpressionTree expr) {
    expr.accept(new StringConcatenationVisitor());
  }

  private class StringConcatenationVisitor extends PHPVisitorCheck {
    @Override
    public void visitBinaryExpression(BinaryExpressionTree tree) {
      if (tree.is(Tree.Kind.CONCATENATION)) {
        checkPathDelimiter(tree.leftOperand());
        checkPathDelimiter(tree.rightOperand());
      }
      super.visitBinaryExpression(tree);
    }

    private void checkPathDelimiter(ExpressionTree expr) {
      ExpressionTree newExpr = CheckUtils.skipParenthesis(expr);
      if (newExpr.is(REGULAR_STRING_LITERAL) && PATH_DELIMETERS_PATTERN.matcher(((LiteralTree) newExpr).value()).find()) {
        HardCodedUriCheck.this.context().newIssue(HardCodedUriCheck.this, newExpr, "Remove this hard-coded path-delimiter.");
      }
    }
  }

}
