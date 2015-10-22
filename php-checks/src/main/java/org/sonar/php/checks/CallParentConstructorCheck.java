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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;

@Rule(
  key = CallParentConstructorCheck.KEY,
  name = "PHP 4 style calls to parent constructors should not be used in PHP5 \"__construct\" functions",
  priority = Priority.MAJOR,
  tags = {Tags.CONVENTION})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.COMPILER_RELATED_PORTABILITY)
@SqaleConstantRemediation("2min")
public class CallParentConstructorCheck extends PHPVisitorCheck {

  public static final String KEY = "S1605";
  private static final String MESSAGE = "Replace \"parent::%s(...)\" by \"parent::__construct(...)\".";

  private String superClass = null;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Kind.CLASS_DECLARATION) && tree.superClass() != null) {
      MethodDeclarationTree constructor = tree.fetchConstructor();

      if (constructor != null && isPHP5Constructor(constructor)) {
        superClass = tree.superClass().fullName();
        scan(constructor);
        superClass = null;
      }
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (superClass != null && tree.callee().is(Kind.CLASS_MEMBER_ACCESS)) {
      MemberAccessTree memberAccess = (MemberAccessTree) tree.callee();
      String memberName = getName(memberAccess.member());

      if (isParent(memberAccess.object()) && superClass.equalsIgnoreCase(memberName)) {
        context().newIssue(KEY, String.format(MESSAGE, memberName)).tree(tree);
      }

    }
    super.visitFunctionCall(tree);
  }

  private static boolean isPHP5Constructor(MethodDeclarationTree constructor) {
    return ClassDeclarationTree.PHP5_CONSTRUCTOR_NAME.equalsIgnoreCase(constructor.name().text());
  }

  @Nullable
  private static String getName(Tree member) {
    // Skipping other complex expression
    return member.is(Kind.NAME_IDENTIFIER) ? ((NameIdentifierTree) member).text() : null;
  }

  private static boolean isParent(ExpressionTree object) {
    // Skipping other complex expression
    return object.is(Kind.NAMESPACE_NAME)
      && "parent".equalsIgnoreCase(((NamespaceNameTree) object).fullName());
  }

}
