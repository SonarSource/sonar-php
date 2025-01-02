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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = SwitchWithoutDefaultCheck.KEY)
public class SwitchWithoutDefaultCheck extends PHPVisitorCheck {

  public static final String KEY = "S131";

  @Override
  public void visitSwitchStatement(SwitchStatementTree switchTree) {
    if (switchTree.cases().stream().noneMatch(clause -> clause.is(Kind.DEFAULT_CLAUSE))) {
      context().newIssue(this, switchTree.switchToken(), "Add a \"case default\" clause to this \"switch\" statement.");
    }
    super.visitSwitchStatement(switchTree);
  }
}
