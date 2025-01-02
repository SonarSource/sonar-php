/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = LowerCaseColorCheck.KEY)
public class LowerCaseColorCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2038";
  private static final String MESSAGE = "Replace \"%s\" with \"%s\".";

  private static final Pattern COLOR_REGEXP = Pattern.compile("#[A-Fa-f0-9]{3,6}");
  private static final Pattern COLOR_REGEXP_UPPER_CASE = Pattern.compile("#[A-F0-9]{3,6}");

  @Override
  public List<Kind> nodesToVisit() {
    return Collections.singletonList(Kind.REGULAR_STRING_LITERAL);
  }

  @Override
  public void visitNode(Tree tree) {
    String stringContent = getStringContent((LiteralTree) tree);

    if (isLowerCaseColor(stringContent)) {
      String message = String.format(MESSAGE, stringContent, stringContent.toUpperCase(Locale.ENGLISH));
      context().newIssue(this, tree, message);
    }
  }

  private static boolean isLowerCaseColor(String str) {
    return COLOR_REGEXP.matcher(str).matches() && !COLOR_REGEXP_UPPER_CASE.matcher(str).matches();
  }

  private static String getStringContent(LiteralTree stringLiteral) {
    String stringContent = stringLiteral.value();
    return stringContent.substring(1, stringContent.length() - 1);
  }
}
