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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.VariableVariableTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = VariableVariablesCheck.KEY)
public class VariableVariablesCheck extends PHPVisitorCheck {

  public static final String KEY = "S1599";
  private static final String MESSAGE = "Remove the use of this variable variable \"%s\".";

  @Override
  public void visitVariableVariable(VariableVariableTree tree) {
    context().newIssue(this, tree, String.format(MESSAGE, tree.toString()));
    super.visitVariableVariable(tree);
  }

}
