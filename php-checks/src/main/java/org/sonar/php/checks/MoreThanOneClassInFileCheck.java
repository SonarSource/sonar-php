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
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = MoreThanOneClassInFileCheck.KEY)
public class MoreThanOneClassInFileCheck extends PHPVisitorCheck {

  public static final String KEY = "S1996";
  private static final String MESSAGE = "There are %s%s%sin this file; move all but one of them to other files.";

  private int nbClass = 0;
  private int nbInterface = 0;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);

    if (tree.is(Kind.CLASS_DECLARATION)) {
      nbClass++;

    } else if (tree.is(Kind.INTERFACE_DECLARATION)) {
      nbInterface++;
    }
  }

  @Override
  public void visitScript(ScriptTree tree) {
    nbClass = 0;
    nbInterface = 0;

    super.visitScript(tree);

    if ((nbClass + nbInterface) > 1) {
      String independentClasses = nbClass > 0 ? (nbClass + " independent classes ") : "";
      String and = nbClass > 0 && nbInterface > 0 ? "and " : "";
      String indendentInterfaces = nbInterface > 0 ? (nbInterface + " independent interfaces ") : ""; 
      String message = String.format(MESSAGE, independentClasses, and, indendentInterfaces);

      int cost = nbClass + nbInterface - 1;
      context().newFileIssue(this, message).cost(cost);
    }
  }

}
