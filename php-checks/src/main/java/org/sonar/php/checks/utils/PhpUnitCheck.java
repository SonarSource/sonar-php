/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks.utils;

import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public abstract class PhpUnitCheck extends PHPVisitorCheck {

  private boolean isPhpUnitTestCase = false;
  private boolean isPhpUnitTestMethod = false;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    isPhpUnitTestCase = isSubClassOfTestCase(tree);
    if (isPhpUnitTestCase) {
      visitPhpUnitTestCase(tree);
    }

    super.visitClassDeclaration(tree);

    isPhpUnitTestCase = false;
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    isPhpUnitTestMethod = isTestCaseMethod(tree);
    if (isPhpUnitTestMethod) {
      visitPhpUnitTestMethod(tree);
    }

    super.visitMethodDeclaration(tree);

    isPhpUnitTestMethod = false;
  }

  protected boolean isTestCaseMethod(MethodDeclarationTree tree) {
    return isPhpUnitTestCase && CheckUtils.isPublic(tree)
      && (tree.name().text().startsWith("test") || CheckUtils.hasAnnotation(tree, "test"));
  }

  protected static boolean isSubClassOfTestCase(ClassDeclarationTree tree) {
    ClassSymbol symbol = Symbols.get(tree);
    return symbol.isSubTypeOf(qualifiedName("PHPUnit\\Framework\\TestCase")).isTrue()
       || symbol.isSubTypeOf(qualifiedName("PHPUnit_Framework_TestCase")).isTrue();
  }

  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    // can be specified in child check
  }

  protected void visitPhpUnitTestMethod(MethodDeclarationTree tree) {
    // can be specified in child check
  }

  public boolean isPhpUnitTestCase() {
    return isPhpUnitTestCase;
  }

  public boolean isPhpUnitTestMethod() {
    return isPhpUnitTestMethod;
  }
}
