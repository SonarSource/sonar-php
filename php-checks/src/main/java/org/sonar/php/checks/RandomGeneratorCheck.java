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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S2245")
public class RandomGeneratorCheck extends FunctionUsageCheck {

  private static final String MESSAGE = "Make sure that using this pseudorandom number generator is safe here.";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Set.of("rand", "mt_rand");
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    context().newIssue(this, tree, MESSAGE);
  }

}
