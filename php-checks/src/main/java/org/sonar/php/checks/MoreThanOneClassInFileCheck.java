/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleLinearRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = MoreThanOneClassInFileCheck.KEY,
  name = "Files should contain only one class or interface each",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleLinearRemediation(coeff = "10min", effortToFixDescription = "classes + interfaces -1")
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
      String message = String.format(MESSAGE,
        nbClass > 0 ? (nbClass + " independent classes ") : "",
        nbClass > 0 && nbInterface > 0 ? "and " : "",
        nbInterface > 0 ? (nbInterface + " independent interfaces ") : "");

      int cost = nbClass + nbInterface - 1;
      context().newIssue(KEY, message).cost(cost);
    }
  }

}
