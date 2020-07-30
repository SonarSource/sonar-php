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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S5863")
public class AssertionCompareToSelfCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Replace this assertion to not have the same actual and expected expression.";

  public AssertionCompareToSelfCheck() {
    super(true);
  }

  @Override
  protected void visitPhpUnitAssertion(FunctionCallTree tree, Assertion assertion) {
    if(assertion.hasExpectedValue() && compareToSelf(tree.arguments())) {
      context().newIssue(this, tree.arguments().get(1), MESSAGE)
        .secondary(tree.arguments().get(0), null);
    }
  }

  private boolean compareToSelf(List<ExpressionTree> args) {
    if (args.size() >= 2) {
      Symbol expectedSymbol = getSymbol(args.get(0));
      return expectedSymbol != null && expectedSymbol.equals(getSymbol(args.get(1)));
    }
    return false;
  }

  private Symbol getSymbol(ExpressionTree tree) {
    return context().symbolTable().getSymbol(tree);
  }
}
