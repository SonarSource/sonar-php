/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.security;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S4834")
public class PermissionsControlCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that Permissions are controlled safely here.";
  private static final QualifiedName CAKE_BASE_AUTHORIZE_CLASS = qualifiedName("Cake\\Auth\\BaseAuthorize");
  private static final QualifiedName CAKE_CONTROLLER_CLASS = qualifiedName("Cake\\Controller\\Controller");

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    checkClass(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    checkClass(tree);
  }

  private void checkClass(ClassTree tree) {
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      QualifiedName fullyQualifiedSuperclassName = getFullyQualifiedName(superClass);
      if (fullyQualifiedSuperclassName.equals(CAKE_BASE_AUTHORIZE_CLASS)) {
        context().newIssue(this, superClass, MESSAGE);
      } else if (fullyQualifiedSuperclassName.equals(CAKE_CONTROLLER_CLASS)) {
        checkCakeControllerMethods(tree);
      }
    }
  }

  private void checkCakeControllerMethods(ClassTree tree) {
    for (ClassMemberTree member : tree.members()) {
      if (member.is(Tree.Kind.METHOD_DECLARATION)) {
        NameIdentifierTree name = ((MethodDeclarationTree) member).name();
        if ("isAuthorized".equalsIgnoreCase(name.text())) {
          context().newIssue(this, name, MESSAGE);
        }
      }
    }
  }

}
