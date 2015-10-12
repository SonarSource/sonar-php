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
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = ConstructorDependencyInversionCheck.KEY,
  name = "Class constructors should not create other objects",
  priority = Priority.MAJOR,
  tags = {"design"})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
@SqaleConstantRemediation("10min")
public class ConstructorDependencyInversionCheck extends PHPVisitorCheck {

  public static final String KEY = "S2830";
  private static final String MESSAGE = "Remove this creation of object in constructor. Use dependency injection instead.";

  private boolean inConstructor = false;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    MethodDeclarationTree constructor = tree.fetchConstructor();

    if (constructor != null) {
      inConstructor = true;
      scan(constructor);
      inConstructor = false;
    }
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    // Ignore instantiation in throw statement.
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (inConstructor) {
      context().newIssue(KEY, MESSAGE).tree(tree);
    }
    super.visitNewExpression(tree);
  }

}
