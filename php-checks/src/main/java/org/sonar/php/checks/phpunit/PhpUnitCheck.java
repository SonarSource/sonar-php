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
package org.sonar.php.checks.phpunit;

import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public abstract class PhpUnitCheck extends PHPVisitorCheck {

  private boolean isPhpUnitTestCase = false;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    ClassSymbol symbol = Symbols.get(tree);
    isPhpUnitTestCase = !symbol.isUnknownSymbol() && isSubTypeOfTestCase(symbol);

    super.visitClassDeclaration(tree);

    isPhpUnitTestCase = false;
  }

  private boolean isSubTypeOfTestCase(ClassSymbol symbol) {
    return symbol.isSubTypeOf(qualifiedName("PHPUnit\\Framework\\TestCase")).isTrue()
      || symbol.isSubTypeOf(qualifiedName("PHPUnit_Framework_TestCase")).isTrue();
  }

  protected boolean isPhpUnitTestCase() {
    return isPhpUnitTestCase;
  }
}
