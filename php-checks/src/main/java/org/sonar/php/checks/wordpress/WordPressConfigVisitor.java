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
package org.sonar.php.checks.wordpress;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

public abstract class WordPressConfigVisitor extends FunctionUsageCheck {

  @Override
  protected Set<String> functionNames() {
    return Collections.singleton("define");
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (isWpConfigFile() && shouldVisitConfig(tree)) {
      visitConfigDeclaration(tree);
    }
  }

  protected boolean isWpConfigFile() {
    return context().getPhpFile().filename().equals("wp-config.php");
  }

  private boolean shouldVisitConfig(FunctionCallTree tree) {
    return configsToVisit().isEmpty() || configsToVisit().stream().anyMatch(configKey -> isConfigKey(tree, configKey));
  }

  protected static Optional<ExpressionTree> configKey(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "constant_name", 0).map(CallArgumentTree::value);
  }

  protected static Optional<ExpressionTree> configValue(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "value", 1).map(CallArgumentTree::value);
  }

  public static boolean isConfigKey(FunctionCallTree tree, String key) {
    return configKey(tree).filter(argument -> CheckUtils.isStringLiteralWithValue(argument, key)).isPresent();
  }

  protected Set<String> configsToVisit() {
    return Collections.emptySet();
  }

  abstract void visitConfigDeclaration(FunctionCallTree config);
}
