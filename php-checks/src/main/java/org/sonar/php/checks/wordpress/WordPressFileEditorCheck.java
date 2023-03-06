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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key="S6341")
public class WordPressFileEditorCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Plugin and theme files editor is active";

  private FunctionCallTree fileEditConfigTree;
  private FunctionCallTree fileModsConfigTree;

  @Override
  public void visitScript(ScriptTree tree) {
    fileEditConfigTree = null;
    fileModsConfigTree = null;
    super.visitScript(tree);
    if (!fileModsDisallowed()) {
      if (fileEditConfigTree == null) {
        context().newFileIssue(this, MESSAGE);
      } else {
        configValue(fileEditConfigTree).filter(CheckUtils::isFalseValue)
          .ifPresent(value -> newIssue(fileEditConfigTree, MESSAGE));
      }
    }
    fileEditConfigTree = null;
    fileModsConfigTree = null;
  }

  @Override
  protected Set<String> configsToVisit() {
    return new HashSet<>(Arrays.asList("DISALLOW_FILE_EDIT", "DISALLOW_FILE_MODS"));
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    if (isConfigKey(config, "DISALLOW_FILE_EDIT")) {
      fileEditConfigTree = config;
    } else {
      // DISALLOW_FILE_MODS
      fileModsConfigTree = config;
    }
  }

  private boolean fileModsDisallowed() {
    return fileModsConfigTree != null && !configValue(fileModsConfigTree).filter(CheckUtils::isFalseValue).isPresent();
  }
}
