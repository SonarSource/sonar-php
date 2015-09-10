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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2001",
  name = "Functions deprecated in PHP 5 should not be used",
  priority = Priority.MAJOR,
  tags = {Tags.OBSOLETE})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LANGUAGE_RELATED_PORTABILITY)
@SqaleConstantRemediation("15min")
public class PHP5DeprecatedFunctionUsageCheck extends SquidCheck<LexerlessGrammar> {

  private static final ImmutableMap<String, String> NEW_BY_DEPRECATED_FUNCTIONS = ImmutableMap.<String, String>builder()
    .put("call_user_method", "call_user_func()")
    .put("call_user_method_array", "call_user_func_array()")
    .put("define_syslog_variables", "")
    .put("dl", "")
    .put("ereg", "preg_match()")
    .put("ereg_replace", "preg_replace()")
    .put("eregi", "preg_match() with 'i' modifier")
    .put("eregi_replace", "preg_replace() with 'i' modifier")
    .put("set_magic_quotes_runtime", "")
    .put("magic_quotes_runtime", "")
    .put("session_register", "$_SESSION")
    .put("session_unregister", "$_SESSION")
    .put("session_is_registered", "$_SESSION")
    .put("set_socket_blocking", "stream_set_blocking")
    .put("split", "preg_split")
    .put("spliti", "preg_split")
    .put("sql_regcase", "")
    .put("mysql_db_query", "mysql_select_db() and mysql_query()")
    .put("mysql_escape_string", "mysql_real_escape_string")
    .build();

  private static final ImmutableSet<String> LOCALE_CATEGORY_CONSTANTS = ImmutableSet.of(
    "LC_ALL", "LC_COLLATE", "LC_CTYPE", "LC_MONETARY", "LC_NUMERIC", "LC_TIME", "LC_MESSAGES");

  @Override
  public void init() {
    subscribeTo(PHPGrammar.MEMBER_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode memberExpr = astNode.getFirstChild();
    AstNode nextNode = memberExpr.getNextAstNode();
    String calledFunctionName = memberExpr.getTokenOriginalValue();
    String replacement = NEW_BY_DEPRECATED_FUNCTIONS.get(calledFunctionName);


    if (nextNode != null && nextNode.is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {

      if ("setlocale".equals(calledFunctionName)) {
        String category = getCategoryFromStringLiteralParameter(nextNode);

        if (category != null) {
          getContext().createLineViolation(this, "Use the \"{0}\" constant instead of a string literal.", astNode, category);
        }

      } else if (replacement != null) {
        getContext().createLineViolation(this, buildMessage(calledFunctionName, replacement), astNode);
      }
    }
  }

  private String getCategoryFromStringLiteralParameter(AstNode parameterList) {
    String firstParam = parameterList.getFirstChild(PHPPunctuator.LPARENTHESIS).getNextAstNode().getTokenOriginalValue();

    if (firstParam.startsWith("\"") && firstParam.endsWith("\"") || firstParam.startsWith("'") && firstParam.endsWith("'")) {
      String category = firstParam.substring(1, firstParam.length() - 1);

      if (LOCALE_CATEGORY_CONSTANTS.contains(category)) {
        return category;
      }
    }
    return null;
  }

  private String buildMessage(String calledFunctionName, String replacement) {
    if (replacement.isEmpty()) {
      return "Remove this \"" + calledFunctionName + "()\" call.";

    } else {
      return "Replace this \"" + calledFunctionName + "()\" call with a call to \"" + replacement + "\".";
    }
  }

}
