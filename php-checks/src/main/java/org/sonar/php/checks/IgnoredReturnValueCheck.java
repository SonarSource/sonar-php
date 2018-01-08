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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerFunctionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2201")
public class IgnoredReturnValueCheck extends PHPVisitorCheck {

  protected static final Set<String> PURE_FUNCTIONS = new HashSet<>(Arrays.asList(
    // String Functions
    "addcslashes",
    "addslashes",
    "bin2hex",
    "chop",
    "chr",
    "chunk_split",
    "convert_cyr_string",
    "convert_uudecode",
    "convert_uuencode",
    "count_chars",
    "crc32",
    "crypt",
    "explode",
    "get_html_translation_table",
    "hebrev",
    "hebrevc",
    "hex2bin",
    "html_entity_decode",
    "htmlentities",
    "htmlspecialchars_decode",
    "htmlspecialchars",
    "implode",
    "join",
    "lcfirst",
    "levenshtein",
    "localeconv",
    "ltrim",
    "md5",
    "metaphone",
    "money_format",
    "nl_langinfo",
    "nl2br",
    "number_format",
    "ord",
    "quoted_printable_decode",
    "quoted_printable_encode",
    "quotemeta",
    "rtrim",
    "sha1",
    "soundex",
    "sprintf",
    "str_getcsv",
    "str_pad",
    "str_repeat",
    "str_rot13",
    "str_shuffle",
    "str_split",
    "str_word_count",
    "strcasecmp",
    "strchr",
    "strcmp",
    "strcoll",
    "strcspn",
    "strip_tags",
    "stripcslashes",
    "stripos",
    "stripslashes",
    "stristr",
    "strlen",
    "strnatcasecmp",
    "strnatcmp",
    "strncasecmp",
    "strncmp",
    "strpbrk",
    "strpos",
    "strrchr",
    "strrev",
    "strripos",
    "strrpos",
    "strspn",
    "strstr",
    "strtok",
    "strtolower",
    "strtoupper",
    "strtr",
    "substr_compare",
    "substr_count",
    "substr_replace",
    "substr",
    "trim",
    "ucfirst",
    "ucwords",
    "wordwrap",

    // Array Functions
    "array_change_key_case",
    "array_chunk",
    "array_column",
    "array_combine",
    "array_count_values",
    "array_diff_assoc",
    "array_diff_key",
    "array_diff",
    "array_fill_keys",
    "array_fill",
    "array_flip",
    "array_intersect_assoc",
    "array_intersect_key",
    "array_intersect",
    "array_key_exists",
    "array_keys",
    "array_merge_recursive",
    "array_merge",
    "array_pad",
    "array_product",
    "array_rand",
    "array_reduce",
    "array_replace_recursive",
    "array_replace",
    "array_reverse",
    "array_search",
    "array_slice",
    "array_sum",
    "array_unique",
    "array_values",
    "array",
    "compact",
    "count",
    "current",
    "in_array",
    "key_exists",
    "key",
    "pos",
    "range",
    "sizeof",

    // Variable handling Functions
    "boolval",
    "debug_zval_dump",
    "doubleval",
    "empty",
    "floatval",
    "get_defined_vars",
    "get_resource_type",
    "gettype",
    "intval",
    "is_array",
    "is_bool",
    "is_double",
    "is_float",
    "is_int",
    "is_integer",
    "is_iterable",
    "is_long",
    "is_null",
    "is_numeric",
    "is_object",
    "is_real",
    "is_resource",
    "is_scalar",
    "is_string",
    "isset",
    "serialize",
    "strval",
    "unserialize",

    // Classes/Object Functions
    "get_called_class",
    "get_class_methods",
    "get_class_vars",
    "get_class",
    "get_declared_classes",
    "get_declared_interfaces",
    "get_declared_traits",
    "get_object_vars",
    "get_parent_class",
    "is_a",
    "is_subclass_of",
    "method_exists",
    "property_exists",

    // Ctype Functions
    "ctype_alnum",
    "ctype_alpha",
    "ctype_cntrl",
    "ctype_digit",
    "ctype_graph",
    "ctype_lower",
    "ctype_print",
    "ctype_punct",
    "ctype_space",
    "ctype_upper",
    "ctype_xdigit",

    // Filter Functions
    "filter_has_var",
    "filter_id",
    "filter_input_array",
    "filter_input",
    "filter_list",
    "filter_var_array",
    "filter_var",

    // Function handling Functions
    "func_get_arg",
    "func_get_args",
    "func_num_args",
    "function_exists",
    "get_defined_functions"));

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    checkExpression(tree.expression());
    super.visitExpressionStatement(tree);
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    tree.init().forEach(this::checkExpression);
    tree.update().forEach(this::checkExpression);
    super.visitForStatement(tree);
  }

  private void checkExpression(ExpressionTree expressionTree) {
    ExpressionTree expression = CheckUtils.skipParenthesis(expressionTree);
    if (expression.is(Tree.Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCall = (FunctionCallTree) expression;
      checkName(expressionTree, functionCall.callee(), CheckUtils.getFunctionName(functionCall));
    } else if (expression.is(Tree.Kind.ARRAY_INITIALIZER_FUNCTION)) {
      ArrayInitializerFunctionTree initializer = (ArrayInitializerFunctionTree) expression;
      checkName(expressionTree, initializer.arrayToken(), "array");
    }
  }

  private void checkName(ExpressionTree expressionTree, Tree issueLocation, @Nullable String name) {
    boolean isPureFunction = name != null && PURE_FUNCTIONS.contains(name.toLowerCase(Locale.ROOT));
    if (isPureFunction && !CheckUtils.isDisguisedShortEchoStatement(expressionTree.getParent())) {
      context().newIssue(this, issueLocation, "The return value of \"" + name + "\" must be used.");
    }
  }

}
