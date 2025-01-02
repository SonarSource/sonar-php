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

import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.sonar.php.checks.utils.CheckUtils.getFunctionName;

@Rule(key = InsecureHashCheck.KEY)
public class InsecureHashCheck extends FunctionUsageCheck {

  public static final String KEY = "S2070";
  private static final String MESSAGE = "Use a stronger hashing algorithm than %s.";

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Set.of("md5", "sha1");
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    String functionName = getFunctionName(tree);
    context().newIssue(this, tree.callee(), String.format(MESSAGE, functionName.toUpperCase(Locale.ENGLISH)));
  }

}
