/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.wordpress;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

@Rule(key = "S6339")
public class WordPressSaltsCheck extends WordPressConfigVisitor {

  private static final String DEFAULT_VALUE_MESSAGE = "Using a default value is insecure.";
  private static final String SHORT_LENGTH_MESSAGE = "Using a short value is insecure.";
  private static final String EMPTY_MESSAGE = "Using an empty value is insecure.";

  private static final String DEFAULT_VALUE = "put your unique phrase here";
  private static final int MIN_LENGTH = 10;

  @Override
  protected Set<String> configsToVisit() {
    return new HashSet<>(Arrays.asList("AUTH_KEY",
      "AUTH_SALT",
      "LOGGED_IN_KEY",
      "LOGGED_IN_SALT",
      "NONCE_KEY",
      "NONCE_SALT",
      "SECURE_AUTH_KEY",
      "SECURE_AUTH_SALT"));
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configValue(config)
      .filter(valueExpression -> valueExpression.is(Tree.Kind.REGULAR_STRING_LITERAL))
      .ifPresent(valueExpression -> checkValue(config, CheckUtils.trimQuotes(((LiteralTree) valueExpression).value())));
  }

  private void checkValue(FunctionCallTree defineTree, String value) {
    if (value.trim().isEmpty()) {
      context().newIssue(this, defineTree, EMPTY_MESSAGE);
    } else if (DEFAULT_VALUE.equalsIgnoreCase(value)) {
      context().newIssue(this, defineTree, DEFAULT_VALUE_MESSAGE);
    } else if (value.length() < MIN_LENGTH) {
      context().newIssue(this, defineTree, SHORT_LENGTH_MESSAGE);
    }
  }
}
