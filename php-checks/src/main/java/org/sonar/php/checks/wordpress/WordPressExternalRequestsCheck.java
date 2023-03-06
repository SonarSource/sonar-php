/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6345")
public class WordPressExternalRequestsCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Make sure allowing external requests is intended.";
  private boolean configOccurred;

  @Override
  public void visitScript(ScriptTree tree) {
    configOccurred = false;
    super.visitScript(tree);
    if (!configOccurred) {
      context().newFileIssue(this, MESSAGE);
    }
  }

  @Override
  protected Set<String> configsToVisit() {
    return Collections.singleton("WP_HTTP_BLOCK_EXTERNAL");
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configOccurred = true;
    configValue(config).filter(CheckUtils::isFalseValue).ifPresent(v -> newIssue(config, MESSAGE));
  }
}
