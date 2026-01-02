/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.Locale;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.php.utils.collections.MapBuilder;
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

  private static final Map<String, String> ALIAS_FUNCTIONS = MapBuilder.<String, String>builder()
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
      String name = ((NamespaceNameTree) callee).qualifiedName();
      String replacementName = ALIAS_FUNCTIONS.get(name.toLowerCase(Locale.ROOT));
      if (replacementName != null) {
        context().newIssue(this, callee, String.format(MESSAGE, name, replacementName));
      }
    }

    super.visitFunctionCall(tree);
  }

}
