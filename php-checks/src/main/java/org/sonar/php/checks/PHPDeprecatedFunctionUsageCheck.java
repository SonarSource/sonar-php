/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.checks.utils.type.NewObjectCall;
import org.sonar.php.checks.utils.type.ObjectMemberFunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

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
    .put("mbregex_encoding", "mb_regex_encoding")
    .put("mbereg", "mb_ereg")
    .put("mberegi", "mb_eregi")
    .put("mbereg_replace", "mb_ereg_replace")
    .put("mberegi_replace", "mb_eregi_replace")
    .put("mbsplit", "mb_split")
    .put("mbereg_match", "mb_ereg_match")
    .put("mbereg_search", "mb_ereg_search")
    .put("mbereg_search_pos", "mb_ereg_search_pos")
    .put("mbereg_search_regs", "mb_ereg_search_regs")
    .put("mbereg_search_init", "mb_ereg_search_init")
    .put("mbereg_search_getregs", "mb_ereg_search_getregs")
    .put("mbereg_search_getpos", "mb_ereg_search_getpos")
    .put("mbereg_search_setpos", "mb_ereg_search_setpos")
    .put("fgetss", "")
    .put("gzgetss", "")
    .build();

  private static final String SET_LOCALE_FUNCTION = "setlocale";
  private static final String PARSE_STR_FUNCTION = "parse_str";
  private static final String ASSERT_FUNCTION = "assert";
  private static final String DEFINE_FUNCTION = "define";
  private static final String STREAM_FILTER_APPEND_FUNCTION = "stream_filter_append";
  private static final ImmutableSet<String> LOCALE_CATEGORY_CONSTANTS = ImmutableSet.of(
    "LC_ALL", "LC_COLLATE", "LC_CTYPE", "LC_MONETARY", "LC_NUMERIC", "LC_TIME", "LC_MESSAGES");

  private static final ImmutableSet<String> DEPRECATED_CASE_SENSITIVE_CONSTANTS = ImmutableSet.of(
    "FILTER_FLAG_SCHEME_REQUIRED", "FILTER_FLAG_HOST_REQUIRED");

  private static final Predicate<TreeValues> SPLFILEOBJECT_FGETSS = new ObjectMemberFunctionCall("fgetss", new NewObjectCall("SplFileObject"));

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.<String>builder()
      .addAll(NEW_BY_DEPRECATED_FUNCTIONS.keySet())
      .add(SET_LOCALE_FUNCTION)
      .add(PARSE_STR_FUNCTION)
      .add(ASSERT_FUNCTION)
      .add(DEFINE_FUNCTION)
      .add(STREAM_FILTER_APPEND_FUNCTION)
      .build();
  }
  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    TreeValues possibleValues = TreeValues.of(tree, context().symbolTable());
    if (SPLFILEOBJECT_FGETSS.test(possibleValues)) {
      context().newIssue(this, tree,"Remove this \"fgetss()\" call.");
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    if (DEPRECATED_CASE_SENSITIVE_CONSTANTS.contains(tree.text())) {
      context().newIssue(this, tree, "Do not use this deprecated " + tree.text() + " constant.");
    }
    super.visitNameIdentifier(tree);
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    String functionName = ((NamespaceNameTree) tree.callee()).qualifiedName();

    if (SET_LOCALE_FUNCTION.equalsIgnoreCase(functionName)) {
      checkLocalCategoryArgument(tree.arguments());

    } else if (PARSE_STR_FUNCTION.equalsIgnoreCase(functionName)) {
      checkParseStrArguments(tree);

    } else if (ASSERT_FUNCTION.equalsIgnoreCase(functionName)) {
      checkAssertArguments(tree);

    } else if (DEFINE_FUNCTION.equalsIgnoreCase(functionName)) {
      checkDefineArguments(tree);

    } else if (STREAM_FILTER_APPEND_FUNCTION.equalsIgnoreCase(functionName)) {
      checkStreamFilterAppendArguments(tree);

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

  private void checkDefineArguments(FunctionCallTree tree) {
    SeparatedList<ExpressionTree> arguments = tree.arguments();
    if (arguments.size() == 3) {
      ExpressionTree caseInsensitiveArgument = arguments.get(2);
      if (caseInsensitiveArgument.is(Kind.BOOLEAN_LITERAL) && "true".equalsIgnoreCase(((LiteralTree) caseInsensitiveArgument).value())) {
        context().newIssue(this, tree, "Define this constant as case sensitive.");
      }
    }
  }

  private void checkStreamFilterAppendArguments(FunctionCallTree tree) {
    SeparatedList<ExpressionTree> arguments = tree.arguments();
    if (arguments.size() >= 2) {
      ExpressionTree filterNameArgument = arguments.get(1);
      if (filterNameArgument.is(Kind.REGULAR_STRING_LITERAL) && CheckUtils.trimQuotes((LiteralTree) filterNameArgument).equals("string.strip_tags")) {
        context().newIssue(this, filterNameArgument, "Remove this highly discouraged 'string.strip_tags' filter usage.");
      }
    }
  }

}
