/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6346")
public class WordPressDbRepairCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Make sure allowing unauthenticated database repair is intended.";

  @Override
  protected Set<String> configsToVisit() {
    return Collections.singleton("WP_ALLOW_REPAIR");
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configValue(config).filter(CheckUtils::isTrueValue).ifPresent(v -> newIssue(config, MESSAGE));
  }
}
