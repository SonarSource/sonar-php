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
package org.sonar.php.checks.utils.argumentmatching;

import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

public class ArgumentVerifierValueContainment extends ArgumentMatcherValueContainment implements FunctionArgumentCheck.IssueRaiser {

  private final boolean raiseIssueOnMatch;

  public ArgumentVerifierValueContainment(ArgumentVerifierValueContainmentBuilder builder) {
    super(builder);
    this.raiseIssueOnMatch = builder.raiseIssueOnMatch;
  }

  @Override
  public boolean shouldRaiseIssue(boolean matchingSuccessful, ExpressionTree argumentValue) {
    return nameOf(argumentValue).isPresent() && matchingSuccessful == raiseIssueOnMatch;
  }

  public boolean isRaiseIssueOnMatch() {
    return raiseIssueOnMatch;
  }

  public static ArgumentVerifierValueContainmentBuilder builder() {
    return new ArgumentVerifierValueContainmentBuilder();
  }

  public static class ArgumentVerifierValueContainmentBuilder extends ArgumentMatcherValueContainmentBuilder<ArgumentVerifierValueContainmentBuilder> {
    private boolean raiseIssueOnMatch = true;

    public ArgumentVerifierValueContainmentBuilder raiseIssueOnMatch(boolean raiseIssueOnMatch) {
      this.raiseIssueOnMatch = raiseIssueOnMatch;
      return this;
    }

    @Override
    public ArgumentVerifierValueContainment build() {
      return new ArgumentVerifierValueContainment(this);
    }
  }
}
