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

import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = CallParentConstructorCheck.KEY)
public class CallParentConstructorCheck extends PHPVisitorCheck {

  public static final String KEY = "S1605";
  private static final String MESSAGE = "Replace \"parent::%s(...)\" by \"parent::__construct(...)\".";

  private String superClass = null;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    if (tree.is(Kind.CLASS_DECLARATION)) {
      visitClass(tree);
    }
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    visitClass(tree);
  }

  private void visitClass(ClassTree tree) {
    if (tree.superClass() != null) {
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
        context().newIssue(this, tree, String.format(MESSAGE, memberName));
      }

    }
    super.visitFunctionCall(tree);
  }

  private static boolean isPHP5Constructor(MethodDeclarationTree constructor) {
    return ClassTree.PHP5_CONSTRUCTOR_NAME.equalsIgnoreCase(constructor.name().text());
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
