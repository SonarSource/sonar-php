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
import java.util.Locale;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * @author Piotr Dawidiuk
 */
@Rule(key = AliasFunctionUsageCheck.KEY)
public class AliasFunctionUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S2050";

  public static final String MESSAGE = "Replace this use of \"%s\" with \"%s\".";

  private static final Map<String, String> ALIAS_FUNCTIONS = ImmutableMap.<String, String>builder()
    .put("chop", "rtrim")
    .put("close", "closedir")
    .put("doubleval", "floatval")
    .put("fputs", "fwrite")
    .put("ini_alter", "ini_set")
    .put("is_double", "is_float")
    .put("is_integer", "is_int")
    .put("is_long", "is_int")
    .put("is_real", "is_float")
    .put("is_writeable", "is_writable")
    .put("join", "implode")
    .put("key_exists", "array_key_exists")
    .put("magic_quotes_runtime", "set_magic_quotes_runtime")
    .put("pos", "current")
    .put("show_source", "highlight_file")
    .put("sizeof", "count")
    .put("strchr", "strstr")
    .build();

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
      String name = ((NamespaceNameTree) callee).fullName();
      String replacementName = ALIAS_FUNCTIONS.get(name.toLowerCase(Locale.ROOT));
      if (replacementName != null) {
        context().newIssue(this, callee, String.format(MESSAGE, name, replacementName));
      }
    }

    super.visitFunctionCall(tree);
  }

}
