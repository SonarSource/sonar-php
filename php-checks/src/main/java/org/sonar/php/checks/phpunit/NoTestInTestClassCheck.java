/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.phpunit;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.tree.impl.declaration.ClassDeclarationTreeImpl;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;

@Rule(key = "S2187")
public class NoTestInTestClassCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Add some tests to this class.";

  @Override
  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    if (!tree.isAbstract() && !hasSuperClassWithTestMethod(tree)) {
      newIssue(tree.name(), MESSAGE);
    }

    super.visitPhpUnitTestCase(tree);
  }

  private boolean hasSuperClassWithTestMethod(ClassDeclarationTree tree) {
    ClassSymbol symbol = ((ClassDeclarationTreeImpl) tree).symbol();
    // as allSuperTypes() contains the ClassSymbol of the tree itself, this is sufficient to check
    return symbol.allSuperTypes().stream().anyMatch(this::hasTestMethod);
  }
}
