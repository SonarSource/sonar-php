/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;

import static org.sonar.php.checks.utils.CheckUtils.getLowerCaseClassName;

@Rule(key = "S3360")
public class TestClassNameCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Rename this class to end with \"Test\" to ensure it will be executed by the PHPUnit CLI.";

  @Override
  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    if (!tree.isAbstract() && !getLowerCaseClassName(tree).endsWith("test")) {
      newIssue(tree.name(), MESSAGE);
    }
  }
}
