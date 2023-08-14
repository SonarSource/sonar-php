/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.checks.utils.argumentmatching;

import java.util.function.Function;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

public class ArgumentVerifierUnaryFunction extends ArgumentMatcher implements FunctionArgumentCheck.IssueRaiser {

  private final Function<ExpressionTree, Boolean> matchingFunction;

  public ArgumentVerifierUnaryFunction(ArgumentVerifierUnaryFunctionBuilder builder) {
    super(builder);
    this.matchingFunction = builder.matchingFunction;
  }

  @Override
  public boolean matches(ExpressionTree argumentValue) {
    return matchingFunction.apply(argumentValue);
  }

  @Override
  public boolean shouldRaiseIssue(boolean matchingSuccessful, ExpressionTree argumentValue) {
    return matchingSuccessful;
  }

  Function<ExpressionTree, Boolean> getMatchingFunction() {
    return matchingFunction;
  }

  public static ArgumentVerifierUnaryFunctionBuilder builder() {
    return new ArgumentVerifierUnaryFunctionBuilder();
  }

  public static class ArgumentVerifierUnaryFunctionBuilder extends ArgumentMatcherBuilder<ArgumentVerifierUnaryFunctionBuilder> {
    private Function<ExpressionTree, Boolean> matchingFunction;

    public ArgumentVerifierUnaryFunctionBuilder matchingFunction(Function<ExpressionTree, Boolean> matchingFunction) {
      this.matchingFunction = matchingFunction;
      return this;
    }

    @Override
    public ArgumentVerifierUnaryFunction build() {
      return new ArgumentVerifierUnaryFunction(this);
    }
  }
}
