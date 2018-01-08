/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

@Rule(key = PHPDeprecatedFunctionUsageCheck.KEY)
public class PHPDeprecatedFunctionUsageCheck extends FunctionUsageCheck {

  public static final String KEY = "S2001";
  private static final String MESSAGE_SET_LOCAL_ARG = "Use the \"%s\" constant instead of a string literal.";
  private static final String MESSAGE_WITH_REPLACEMENT = "Replace this \"%s()\" call with a call to \"%s\".";
  private static final String MESSAGE_WITHOUT_REPLACEMENT = "Remove this \"%s()\" call.";
  private static final String SESSION = "$_SESSION";

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
    .put("session_register", SESSION)
    .put("session_unregister", SESSION)
    .put("session_is_registered", SESSION)
    .put("set_socket_blocking", "stream_set_blocking")
    .put("split", "preg_split")
    .put("spliti", "preg_split")
    .put("sql_regcase", "")
    .put("mysql_db_query", "mysql_select_db() and mysql_query()")
    .put("mysql_escape_string", "mysql_real_escape_string")
    .put("__autoload", "spl_autoload_register")
    .put("create_function", "")
    .put("gmp_random", "gmp_random_bits")
    .put("each", "")
    .build();

  private static final String SET_LOCALE_FUNCTION = "setlocale";
  private static final String PARSE_STR_FUNCTION = "parse_str";
  private static final String ASSERT_FUNCTION = "assert";
  private static final ImmutableSet<String> LOCALE_CATEGORY_CONSTANTS = ImmutableSet.of(
    "LC_ALL", "LC_COLLATE", "LC_CTYPE", "LC_MONETARY", "LC_NUMERIC", "LC_TIME", "LC_MESSAGES");

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.<String>builder()
      .addAll(NEW_BY_DEPRECATED_FUNCTIONS.keySet())
      .add(SET_LOCALE_FUNCTION)
      .add(PARSE_STR_FUNCTION)
      .add(ASSERT_FUNCTION)
      .build();
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    String functionName = ((NamespaceNameTree) tree.callee()).qualifiedName();

    if (SET_LOCALE_FUNCTION.equals(functionName)) {
      checkLocalCategoryArgument(tree.arguments());

    } else if (PARSE_STR_FUNCTION.equals(functionName)) {
      checkParseStrArguments(tree);

    } else if (ASSERT_FUNCTION.equals(functionName)) {
      checkAssertArguments(tree);

    } else {
      context().newIssue(this, tree.callee(), buildMessage(functionName));
    }

  }

  /**
   * Build issue message depending on the presence of a replacement function of not.
   */
  private static String buildMessage(String functionName) {
    String replacement = NEW_BY_DEPRECATED_FUNCTIONS.get(functionName);

    return replacement.isEmpty() ?
      String.format(MESSAGE_WITHOUT_REPLACEMENT, functionName) :
      String.format(MESSAGE_WITH_REPLACEMENT, functionName, replacement);
  }

  /**
   * Raise an issue if the local category is passed as a String.
   */
  private void checkLocalCategoryArgument(SeparatedList<ExpressionTree> arguments) {
    if (!arguments.isEmpty() && arguments.get(0).is(Kind.REGULAR_STRING_LITERAL)) {
      String firstArg = ((LiteralTree) arguments.get(0)).value();
      String localCategory = firstArg.substring(1, firstArg.length() - 1);

      if (LOCALE_CATEGORY_CONSTANTS.contains(localCategory)) {
        context().newIssue(this, arguments.get(0), String.format(MESSAGE_SET_LOCAL_ARG, localCategory));
      }
    }
  }

  private void checkParseStrArguments(FunctionCallTree tree) {
    if (tree.arguments().size() < 2) {
      context().newIssue(this, tree, "Add a second argument to this call to \"parse_str\".");
    }
  }

  private void checkAssertArguments(FunctionCallTree tree) {
    SeparatedList<ExpressionTree> arguments = tree.arguments();
    if (!arguments.isEmpty() && arguments.get(0).is(Kind.REGULAR_STRING_LITERAL, Kind.EXPANDABLE_STRING_LITERAL)) {
      context().newIssue(this, tree, "Change this call to \"assert\" to not pass a string argument.");
    }
  }

}
