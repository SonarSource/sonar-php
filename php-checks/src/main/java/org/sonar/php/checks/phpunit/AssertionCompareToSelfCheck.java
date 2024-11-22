/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5863")
public class AssertionCompareToSelfCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Replace this assertion to not have the same actual and expected expression.";

  @Override
  protected void visitPhpUnitAssertion(FunctionCallTree tree, Assertion assertion) {
    if (assertion.hasExpectedValue() && compareToSelf(tree.callArguments())) {
      List<CallArgumentTree> args = tree.callArguments();
      newIssue(args.get(1).value(), MESSAGE).secondary(args.get(0).value(), null);
    }
  }

  private boolean compareToSelf(List<CallArgumentTree> args) {
    if (args.size() >= 2) {
      Symbol expectedSymbol = getSymbol(args.get(0).value());
      return expectedSymbol != null && expectedSymbol.equals(getSymbol(args.get(1).value()));
    }
    return false;
  }

  private Symbol getSymbol(ExpressionTree tree) {
    return context().symbolTable().getSymbol(tree);
  }
}
