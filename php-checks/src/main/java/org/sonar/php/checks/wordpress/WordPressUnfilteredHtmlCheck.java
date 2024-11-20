/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.wordpress;

import java.util.Collections;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6348")
public class WordPressUnfilteredHtmlCheck extends WordPressConfigVisitor {
  private static final String MESSAGE = "Make sure allowing unfiltered HTML is intended.";
  private boolean configOccurred;

  @Override
  protected Set<String> configsToVisit() {
    return Collections.singleton("DISALLOW_UNFILTERED_HTML");
  }

  @Override
  public void visitScript(ScriptTree tree) {
    configOccurred = false;
    super.visitScript(tree);
    if (!configOccurred) {
      context().newFileIssue(this, MESSAGE);
    }
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configOccurred = true;
    configValue(config).filter(CheckUtils::isFalseValue).ifPresent(v -> newIssue(config, MESSAGE));
  }
}
