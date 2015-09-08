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
package org.sonar.php.checks.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.apache.commons.lang.StringUtils;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.sslr.grammar.GrammarRuleKey;

import java.util.Arrays;
import java.util.List;

public class FunctionUtils {

  private static final GrammarRuleKey[] FUNCTIONS = {
    PHPGrammar.METHOD_DECLARATION,
    PHPGrammar.FUNCTION_DECLARATION,
    PHPGrammar.FUNCTION_EXPRESSION};

  private FunctionUtils() {
  }

  /**
   * Returns function or method's name, or "expression" if the given node is a function expression.
   *
   * @param functionDec FUNCTION_DECLARATION, METHOD_DECLARATION or FUNCTION_EXPRESSION
   * @return name of function or "expression" if function expression
   */
  public static String getFunctionName(AstNode functionDec) {
    Preconditions.checkArgument(functionDec.is(PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.FUNCTION_EXPRESSION));
    return functionDec.is(PHPGrammar.FUNCTION_EXPRESSION) ? "expression" : ("\"" + functionDec.getFirstChild(PHPGrammar.IDENTIFIER).getTokenOriginalValue() + "\"");
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
   * Return wether the method is overriding a parent method or not.
   *
   * @param methodDec METHOD_DECLARATION
   * @return true if method has tag "@inheritdoc" in it's doc comment.
   */
  public static boolean isOverriding(AstNode methodDec) {
    Token functionToken = methodDec.getToken();
    for (Trivia comment : functionToken.getTrivia()) {
      if (StringUtils.containsIgnoreCase(comment.getToken().getValue(), "@inheritdoc")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test whether a non-private method is owned by a class declared with "extends" or "implements".
   *
   * @param methodDec METHOD_DECLARATION
   * @return true if the node is a non-private method declaration inside a class declared with "extends" or "implements"
   */
  public static boolean mayOverrideAnotherMethod(AstNode methodDec) {
    if (methodDec.is(PHPGrammar.METHOD_DECLARATION) && !hasModifier(methodDec, PHPKeyword.PRIVATE)) {
      AstNode classDec = methodDec.getParent().getParent();
      return classDec.hasDirectChildren(PHPGrammar.EXTENDS_FROM, PHPGrammar.IMPLEMENTS_LIST);
    }
    return false;
  }

  private static boolean hasModifier(AstNode methodDec, PHPKeyword modifier) {
    return methodDec.select()
      .children(PHPGrammar.MEMBER_MODIFIER)
      .children(modifier)
      .isNotEmpty();
  }

  /**
   * Returns an array of GrammarRuleKey containing function LexerlessGrammar rule:
   * METHOD_DECLARATION, FUNCTION_DECLARATION, FUNCTION_EXPRESSION.
   */
  public static GrammarRuleKey[] functions() {
    return Arrays.copyOf(FUNCTIONS, FUNCTIONS.length);
  }

  /**
   * Returns list of parameters for the given methods, function or anonymous function.
   *
   * @param functionDec is FUNCTION_DECLARATION, METHOD_DECLARATION or FUNCTION_EXPRESSION
   * @return list of VARIABLE_IDENTIFIER
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

}
