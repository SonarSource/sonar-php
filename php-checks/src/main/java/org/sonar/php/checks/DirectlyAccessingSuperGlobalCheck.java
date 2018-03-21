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
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = DirectlyAccessingSuperGlobalCheck.KEY)
public class DirectlyAccessingSuperGlobalCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Do not access \"%s\" directly.";
  public static final String KEY = "S2043";

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    checkVariable(tree.variableExpression().token());
    super.visitVariableIdentifier(tree);
  }

  private void checkVariable(SyntaxToken variable) {
    String name = variable.text();

    if (CheckUtils.SUPERGLOBALS_BY_OLD_NAME.values().contains(name) && !"$_SESSION".equals(name)) {
      context().newIssue(this, variable, String.format(MESSAGE, name));
    }
  }

}
