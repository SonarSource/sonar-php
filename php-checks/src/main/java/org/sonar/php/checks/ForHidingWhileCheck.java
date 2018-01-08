/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ForHidingWhileCheck.KEY)
public class ForHidingWhileCheck extends PHPVisitorCheck {

  public static final String KEY = "S1264";
  private static final String MESSAGE = "Replace this \"for\" loop with a \"while\" loop.";

  @Override
  public void visitForStatement(ForStatementTree tree) {
    super.visitForStatement(tree);

    if (tree.init().isEmpty() && tree.update().isEmpty()) {
      context().newIssue(this, tree.forToken(), MESSAGE);
    }
  }

}
