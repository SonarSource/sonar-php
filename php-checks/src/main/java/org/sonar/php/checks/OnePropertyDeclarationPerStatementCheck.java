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
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = OnePropertyDeclarationPerStatementCheck.KEY)
public class OnePropertyDeclarationPerStatementCheck extends PHPVisitorCheck {

  public static final String KEY = "S1766";
  private static final String MESSAGE = "%s property declarations were found in this statement. Reformat the code to declare only one property per statement.";

  @Override
  public void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    super.visitClassPropertyDeclaration(tree);

    int declarationsNumber = tree.declarations().size();

    if (declarationsNumber > 1) {
      String message = String.format(MESSAGE, declarationsNumber);
      context().newIssue(this, tree, message);
    }
  }

}
