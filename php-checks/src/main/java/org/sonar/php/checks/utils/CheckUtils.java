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
package org.sonar.php.checks.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.api.PHPTokenType;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.sslr.grammar.GrammarRuleKey;

import java.util.List;

public class CheckUtils {

  public static final ImmutableMap<String, String> PREDEFINED_VARIABLES = ImmutableMap.<String, String>builder()
    .put("$HTTP_SERVER_VARS", "$_SERVER")
    .put("$HTTP_GET_VARS", "$_GET")
    .put("$HTTP_POST_VARS", "$_POST")
    .put("$HTTP_POST_FILES", "$_FILES")
    .put("$HTTP_SESSION_VARS", "$_SESSION")
    .put("$HTTP_ENV_VARS", "$_ENV")
    .put("$HTTP_COOKIE_VARS", "$_COOKIE").build();

  private static final ImmutableList<GrammarRuleKey> FUNCTIONS = ImmutableList.<GrammarRuleKey>of(
    PHPGrammar.METHOD_DECLARATION,
    PHPGrammar.FUNCTION_DECLARATION,
    PHPGrammar.FUNCTION_EXPRESSION);

  private CheckUtils() {
  }

  public static boolean isExpressionABooleanLiteral(AstNode expression) {
    Preconditions.checkArgument(expression.is(PHPGrammar.EXPRESSION));
    AstNode postfixExpr = expression.getFirstChild(PHPGrammar.POSTFIX_EXPR);

    if (postfixExpr == null) {
      return false;
    }

    AstNode commonScalar = postfixExpr.getFirstChild(PHPGrammar.COMMON_SCALAR);
    return commonScalar != null && commonScalar.getFirstChild().is(PHPGrammar.BOOLEAN_LITERAL);
  }

  /**
   * Returns function or method's name, or "expression" if the given node is a function expression.
   *
   * @param functionDec FUNCTION_DECLARATION, METHOD_DECLARATION or FUNCTION_EXPRESSION
   * @return name of function or "expression" if function expression
   */
  public static String getFunctionName(AstNode functionDec) {
    Preconditions.checkArgument(functionDec.is(PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.FUNCTION_EXPRESSION));
    return functionDec.is(PHPGrammar.FUNCTION_EXPRESSION) ? "expression" : "\"" + functionDec.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue() + "\"";
  }

  /**
   * Returns whether a method declaration has an implementation or not.
   *
   * @param methodDec METHOD_DECLARATION
   * @return true if method declaration without implementation, false otherwise
   */
  public static boolean isAbstractMethod(AstNode methodDec) {
    return methodDec.is(PHPGrammar.METHOD_DECLARATION)
      && methodDec.getFirstChild(PHPGrammar.METHOD_BODY).getFirstChild().is(PHPPunctuator.SEMICOLON);
  }

  /**
   * Returns an array of GrammarRuleKey containing function LexerlessGrammar rule:
   * METHOD_DECLARATION, FUNCTION_DECLARATION, FUNCTION_EXPRESSION.
   */
  public static GrammarRuleKey[] functions() {
    return FUNCTIONS.toArray(new GrammarRuleKey[FUNCTIONS.size()]);
  }

  /**
   * Returns list of parameters for the given methods, function or anonymous function.
   *
   * @param functionDec is FUNCTION_DECLARATION, METHOD_DECLARATION or FUNCTION_EXPRESSION
   * @return list of VAR_IDENTIFIER
   */
  public static List<AstNode> getFunctionParameters(AstNode functionDec) {
    Preconditions.checkArgument(functionDec.is(PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.FUNCTION_EXPRESSION));

    List<AstNode> parameters = Lists.newArrayList();
    AstNode parameterList = functionDec.getFirstChild(PHPGrammar.PARAMETER_LIST);

    if (parameterList != null) {
      for (AstNode parameter : parameterList.getChildren(PHPGrammar.PARAMETER)) {
        parameters.add(parameter.getFirstChild(PHPGrammar.VAR_IDENTIFIER));
      }
    }
    return parameters;
  }

  /**
   * Return String representing the full expression given as parameter.
   */
  public static String getExpressionAsString(AstNode node) {
    StringBuilder builder = new StringBuilder();
    for (Token token : node.getTokens()) {
      builder.append(token.getOriginalValue());
    }
    return builder.toString();
  }

}
