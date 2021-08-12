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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6347")
public class WordPressLateConfigCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Configuration options at this location will not be taken into account.";
  private boolean endOfConfig;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    if (isWpConfigFile()) {
      endOfConfig = false;
      super.visitCompilationUnit(tree);
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    checkEndOfConfig(tree);
    super.visitFunctionCall(tree);
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    if (endOfConfig) {
      newIssue(config, MESSAGE);
    }
  }

  private void checkEndOfConfig(FunctionCallTree tree) {
    if ("require_once".equalsIgnoreCase(CheckUtils.functionName(tree)) && isSettingsInclusion(tree)) {
      endOfConfig = true;
    }
  }

  private static boolean isSettingsInclusion(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "", 0)
      .map(CallArgumentTree::value)
      .filter(BinaryExpressionTree.class::isInstance)
      .map(a -> ((BinaryExpressionTree) a).rightOperand())
      .filter(a -> CheckUtils.isStringLiteralWithValue(a, "wp-settings.php"))
      .isPresent();
  }
}
