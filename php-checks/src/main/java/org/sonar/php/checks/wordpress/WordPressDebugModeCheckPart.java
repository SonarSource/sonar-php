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
