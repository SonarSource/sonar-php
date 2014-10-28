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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableMap;
import com.sonar.sslr.api.AstNode;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S2001",
  name = "Functions deprecated in PHP 5 should not be used",
  priority = Priority.MINOR)
public class PHP5DeprecatedFunctionUsageCheck extends SquidCheck<LexerlessGrammar> {

  private static final ImmutableMap<String, String> NEW_BY_DEPRECATED_FUNCTIONS = ImmutableMap.<String, String>builder()
    .put("call_user_method", "call_user_func()")
    .put("call_user_method_array", "call_user_func_array()")
    .put("define_syslog_variables", "")
    .put("dl", "")
    .put("ereg", "preg_match()")
    .put("ereg_replace", "preg_replace()")
    .put("eregi)", "preg_match() with 'i' modifier")
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

  @Override
  public void init() {
    subscribeTo(PHPGrammar.MEMBER_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode memberExpr = astNode.getFirstChild();
    String calledFunctionName = memberExpr.getTokenOriginalValue();
    String replacement = NEW_BY_DEPRECATED_FUNCTIONS.get(calledFunctionName);

    if (replacement != null && memberExpr.getNextAstNode().is(PHPGrammar.FUNCTION_CALL_PARAMETER_LIST)) {
      getContext().createLineViolation(this, buildMessage(calledFunctionName, replacement), astNode);
    }
  }

  private String buildMessage(String calledFunctionName, String replacement) {
    if (replacement.isEmpty()) {
      return "Remove this \"" + calledFunctionName + "()\" call.";

    } else {
      return "Replace this \"" + calledFunctionName + "()\" call with a call to \"" + replacement + "\".";
    }
  }

}
