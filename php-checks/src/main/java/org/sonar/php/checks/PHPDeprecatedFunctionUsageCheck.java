/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.php.checks.utils.type.NewObjectCall;
import org.sonar.php.checks.utils.type.ObjectMemberFunctionCall;
import org.sonar.php.checks.utils.type.TreeValues;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

@Rule(key = PHPDeprecatedFunctionUsageCheck.KEY)
public class PHPDeprecatedFunctionUsageCheck extends FunctionUsageCheck {

  public static final String KEY = "S2001";
  private static final String MESSAGE_SET_LOCAL_ARG = "Use the \"%s\" constant instead of a string literal.";
  private static final String MESSAGE_WITH_REPLACEMENT = "Replace this \"%s()\" call with a call to \"%s\".";
  private static final String MESSAGE_WITHOUT_REPLACEMENT = "Remove this call to deprecated \"%s()\".";
  private static final String SESSION = "$_SESSION";
  private static final String FGETSS_FUNCTION = "fgetss";
  private static final String GZGETSS_FUNCTION = "gzgetss";

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
    .put("set_socket_blocking", "stream_set_blocking()")
    .put("split", "preg_split()")
    .put("spliti", "preg_split()")
    .put("sql_regcase", "")
    .put("mysql_db_query", "mysql_select_db() and mysql_query()")
    .put("mysql_escape_string", "mysql_real_escape_string()")
    .put("__autoload", "spl_autoload_register()")
    .put("create_function", "")
    .put("gmp_random", "gmp_random_bits()")
    .put("each", "")
    .put("mbregex_encoding", "mb_regex_encoding()")
    .put("mbereg", "mb_ereg()")
    .put("mberegi", "mb_eregi()")
    .put("mbereg_replace", "mb_ereg_replace()")
    .put("mberegi_replace", "mb_eregi_replace()")
    .put("mbsplit", "mb_split()")
    .put("mbereg_match", "mb_ereg_match()")
    .put("mbereg_search", "mb_ereg_search()")
    .put("mbereg_search_pos", "mb_ereg_search_pos()")
    .put("mbereg_search_regs", "mb_ereg_search_regs()")
    .put("mbereg_search_init", "mb_ereg_search_init()")
    .put("mbereg_search_getregs", "mb_ereg_search_getregs()")
    .put("mbereg_search_getpos", "mb_ereg_search_getpos()")
    .put("mbereg_search_setpos", "mb_ereg_search_setpos()")
    .put(FGETSS_FUNCTION, "")
    .put(GZGETSS_FUNCTION, "")
    .put("image2wbmp", "imagewbmp()")
    .build();

  private static final String SET_LOCALE_FUNCTION = "setlocale";
  private static final String PARSE_STR_FUNCTION = "parse_str";
  private static final String ASSERT_FUNCTION = "assert";
  private static final String DEFINE_FUNCTION = "define";
  private static final Set<String> LOCALE_CATEGORY_CONSTANTS = SetUtils.immutableSetOf(
    "LC_ALL", "LC_COLLATE", "LC_CTYPE", "LC_MONETARY", "LC_NUMERIC", "LC_TIME", "LC_MESSAGES");

  private static final Set<String> DEPRECATED_CASE_SENSITIVE_CONSTANTS = SetUtils.immutableSetOf(
    "FILTER_FLAG_SCHEME_REQUIRED", "FILTER_FLAG_HOST_REQUIRED");

  private static final Set<String> SEARCHING_STRING_FUNCTIONS = SetUtils.immutableSetOf(
    "stristr", "strrchr", "strstr", "strripos", "stripos", "strrpos", "strpos", "strchr");

  private static final Predicate<TreeValues> SPLFILEOBJECT_FGETSS = new ObjectMemberFunctionCall(FGETSS_FUNCTION, new NewObjectCall("SplFileObject"));

  @Override
  protected Set<String> functionNames() {
    Set<String> functionNames = SetUtils.immutableSetOf(SET_LOCALE_FUNCTION, PARSE_STR_FUNCTION, ASSERT_FUNCTION, DEFINE_FUNCTION);
    return SetUtils.concat(functionNames, NEW_BY_DEPRECATED_FUNCTIONS.keySet(), SEARCHING_STRING_FUNCTIONS);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    TreeValues possibleValues = TreeValues.of(tree, context().symbolTable());
    if (SPLFILEOBJECT_FGETSS.test(possibleValues)) {
      context().newIssue(this, tree, String.format(MESSAGE_WITHOUT_REPLACEMENT, FGETSS_FUNCTION));
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    if (DEPRECATED_CASE_SENSITIVE_CONSTANTS.contains(tree.text())) {
      context().newIssue(this, tree, "Do not use this deprecated \"" + tree.text() + "\" constant.");
    }
    super.visitNameIdentifier(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL) && CheckUtils.trimQuotes(tree).equals("string.strip_tags")) {
      context().newIssue(this, tree, "Remove this deprecated \"string.strip_tags\" filter usage.");
    }
    super.visitLiteral(tree);
  }

  @Override
  public void visitMemberAccess(MemberAccessTree tree) {
    if (tree.isStatic() && tree.object().is(Kind.NAMESPACE_NAME) && tree.member().is(Kind.NAME_IDENTIFIER)) {
      String constantType = ((NamespaceNameTree) tree.object()).unqualifiedName();
      String constantName = ((NameIdentifierTree) tree.member()).text();
      if (constantType.equalsIgnoreCase("Normalizer") && constantName.equals("NONE")) {
        context().newIssue(this, tree, "Do not use this deprecated \"Normalizer::NONE\" constant.");
      }
    }
    super.visitMemberAccess(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    NameIdentifierTree name = tree.name();
    if (name.text().equalsIgnoreCase(ASSERT_FUNCTION)) {
      context().newIssue(this, name, "Use the standard \"assert\" function instead of declaring a new assert function.");
    }
    super.visitFunctionDeclaration(tree);
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    String functionName = ((NamespaceNameTree) tree.callee()).qualifiedName();

    if (SET_LOCALE_FUNCTION.equalsIgnoreCase(functionName)) {
      checkLocalCategoryArgument(tree.callArguments());

    } else if (PARSE_STR_FUNCTION.equalsIgnoreCase(functionName)) {
      checkParseStrArguments(tree);

    } else if (ASSERT_FUNCTION.equalsIgnoreCase(functionName)) {
      checkAssertArguments(tree);

    } else if (DEFINE_FUNCTION.equalsIgnoreCase(functionName)) {
      checkDefineArguments(tree);

    } else if (SEARCHING_STRING_FUNCTIONS.contains(functionName.toLowerCase(Locale.ROOT))) {
      checkSearchingStringArguments(tree);

    } else {
      context().newIssue(this, tree.callee(), buildMessage(functionName.toLowerCase(Locale.ROOT)));
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
  private void checkLocalCategoryArgument(SeparatedList<CallArgumentTree> arguments) {
    // PHP 8 doesn't support the deprecated feature anymore, so we don't need to care about named arguments
    if (!arguments.isEmpty() && arguments.get(0).value().is(Kind.REGULAR_STRING_LITERAL)) {
      String firstArg = ((LiteralTree) arguments.get(0).value()).value();
      String localCategory = firstArg.substring(1, firstArg.length() - 1);

      if (LOCALE_CATEGORY_CONSTANTS.contains(localCategory)) {
        context().newIssue(this, arguments.get(0).value(), String.format(MESSAGE_SET_LOCAL_ARG, localCategory));
      }
    }
  }

  private void checkParseStrArguments(FunctionCallTree tree) {
    // PHP 8 doesn't support the deprecated feature anymore, so we don't need to care about named arguments
    if (tree.callArguments().size() < 2) {
      context().newIssue(this, tree, "Add a second argument to this call to \"parse_str\".");
    }
  }

  private void checkAssertArguments(FunctionCallTree tree) {
    Optional<CallArgumentTree> assertion = CheckUtils.argument(tree, "assertion", 0);
    if (assertion.isPresent() && assertion.get().value().is(Kind.REGULAR_STRING_LITERAL, Kind.EXPANDABLE_STRING_LITERAL)) {
      context().newIssue(this, tree, "Change this call to \"assert\" to not pass a string argument.");
    }
  }

  private void checkDefineArguments(FunctionCallTree tree) {
    Optional<CallArgumentTree> caseInsensitiveArg = CheckUtils.argument(tree, "case_insensitive", 2);
    if (caseInsensitiveArg.isPresent()) {
      ExpressionTree argumentValue = caseInsensitiveArg.get().value();
      if (argumentValue.is(Kind.BOOLEAN_LITERAL) && "true".equalsIgnoreCase(((LiteralTree) argumentValue).value())) {
        context().newIssue(this, tree, "Define this constant as case sensitive.");
      }
    }
  }

  private void checkSearchingStringArguments(FunctionCallTree tree) {
    Optional<CallArgumentTree> needleArgument = CheckUtils.argument(tree, "needle", 1);
    if (needleArgument.isPresent()) {
      ExpressionTree needleValue = needleArgument.get().value();
      if (needleValue.is(Kind.NUMERIC_LITERAL, Kind.UNARY_MINUS)) {
        context().newIssue(this, needleValue, "Convert this integer needle into a string.");
      }
    }
  }
}
