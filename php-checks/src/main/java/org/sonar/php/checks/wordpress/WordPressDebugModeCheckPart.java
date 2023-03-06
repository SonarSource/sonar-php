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
import org.sonar.php.checks.CheckBundle;
import org.sonar.php.checks.CheckBundlePart;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

public class WordPressDebugModeCheckPart extends WordPressConfigVisitor implements CheckBundlePart {

  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";
  private CheckBundle bundle;

  @Override
  protected Set<String> configsToVisit() {
    return Collections.singleton("WP_DEBUG");
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configValue(config).filter(CheckUtils::isTrueValue).ifPresent(v -> context().newIssue(getBundle(), config, MESSAGE));
  }

  @Override
  public void setBundle(CheckBundle bundle) {
    this.bundle = bundle;
  }

  @Override
  public CheckBundle getBundle() {
    return bundle;
  }
}
