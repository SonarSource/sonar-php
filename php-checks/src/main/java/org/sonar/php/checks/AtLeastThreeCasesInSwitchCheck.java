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

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = AtLeastThreeCasesInSwitchCheck.KEY)
public class AtLeastThreeCasesInSwitchCheck extends PHPVisitorCheck {

  public static final String KEY = "S1301";

  @Override
  public void visitSwitchStatement(SwitchStatementTree switchTree) {
    if (switchTree.cases().size() < 3) {
      context().newIssue(this, switchTree.switchToken(), "Replace this \"switch\" statement with \"if\" statements to increase readability.");
    }

    super.visitSwitchStatement(switchTree);
  }
}
