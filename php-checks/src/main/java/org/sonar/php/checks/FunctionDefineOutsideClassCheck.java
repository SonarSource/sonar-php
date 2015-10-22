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

import com.google.common.collect.Sets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.Set;

@Rule(
  key = FunctionDefineOutsideClassCheck.KEY,
  name = "Functions and variables should not be defined outside of classes",
  priority = Priority.MAJOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNIT_TESTABILITY)
@SqaleConstantRemediation("15min")
public class FunctionDefineOutsideClassCheck extends PHPVisitorCheck {

  public static final String KEY = "S2007";
  private static final String MESSAGE = "Move this %s into a class.";

  private final Set<String> globalVariableNames = Sets.newHashSet();

  @Override
  public void visitScript(ScriptTree tree) {
    globalVariableNames.clear();
    super.visitScript(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    // don't visit nested nodes
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    // don't visit nested nodes
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    context().newIssue(KEY, String.format(MESSAGE, "function")).tree(tree);
    // don't visit nested nodes
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    super.visitAssignmentExpression(tree);

    if (tree.is(Kind.ASSIGNMENT) && tree.variable().is(Kind.VARIABLE_IDENTIFIER)) {
      String varName = ((VariableIdentifierTree) tree.variable()).variableExpression().text();

      if (!CheckUtils.isSuperGlobal(varName) && !globalVariableNames.contains(varName)) {
        context().newIssue(KEY, String.format(MESSAGE, "variable")).tree(tree);
        globalVariableNames.add(varName);
      }
    }
  }

}
