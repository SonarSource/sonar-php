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
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = NestedFunctionDepthCheck.KEY,
  name = "Functions should not be nested too deeply",
  priority = Priority.MAJOR,
  tags = {Tags.BRAIN_OVERLOAD})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_CHANGEABILITY)
@SqaleConstantRemediation("20min")
public class NestedFunctionDepthCheck extends PHPVisitorCheck {

  public static final String KEY = "S2004";

  private static final String MESSAGE = "Refactor this code to not nest functions more than %s levels deep.";

  private int nestedLevel = 0;
  public static final int DEFAULT = 3;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    nestedLevel = 0;
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    enterFunction(tree);
    super.visitFunctionDeclaration(tree);
    exitFunction();
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    enterFunction(tree);
    super.visitMethodDeclaration(tree);
    exitFunction();
  }

  private void enterFunction(Tree tree) {
    nestedLevel++;
    if (nestedLevel == max + 1) {
      context().newIssue(KEY, String.format(MESSAGE, max)).tree(tree);
    }
  }

  private void exitFunction() {
    nestedLevel--;
  }

}
