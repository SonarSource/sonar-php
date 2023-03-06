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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1821")
public class NestedSwitchCheck extends PHPVisitorCheck {
  public static final String KEY = "S1821";
  private static final String MESSAGE = "Refactor this code to eliminate this nested \"switch\" statement.";
  private static final String MESSAGE_SECONDARY = "Parent \"switch\" statement";
  private SwitchStatementTree parentSwitchStatement = null;

  @Override
  public void visitSwitchStatement(SwitchStatementTree tree) {
    if (parentSwitchStatement != null) {
      context().newIssue(this, tree.switchToken(), MESSAGE)
        .secondary(parentSwitchStatement.switchToken(), MESSAGE_SECONDARY);
    }

    SwitchStatementTree previousParentSwitchStatement = parentSwitchStatement;

    parentSwitchStatement = tree;
    super.visitSwitchStatement(tree);
    parentSwitchStatement = previousParentSwitchStatement;
  }
}
