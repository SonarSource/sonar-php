/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.utils;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

class PhpUnitCheckTest {

  @Test
  void test() {
    CheckVerifier.verify(new PhpUnitCheckImpl(), "utils/PhpUnitCheck.php");
  }

  static class PhpUnitCheckImpl extends PhpUnitCheck {
    @Override
    protected void visitPhpUnitTestMethod(MethodDeclarationTree tree) {
      context().newIssue(this, tree, "Identified as test method.");
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (isPhpUnitTestCase() && isPhpUnitTestMethod() && isAssertion(tree)) {
        context().newIssue(this, tree, "Identified as test assertion.");
      }
      super.visitFunctionCall(tree);
    }
  }
}
