/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * This abstract class simplifies the checking of function calls for the specific arguments.
 * {@link ArgumentVerifier} can be used to check whether an argument corresponds to a certain value or a set of values.
 * Based on a flag of the verifier an issue can be created on match or non-match.
 * If arguments should only be checked when other arguments must match certain values,
 * {@link ArgumentMatcher} can be used. They are used as a condition for the verification.
 */
public abstract class FunctionArgumentCheck extends PHPVisitorCheck {

  /**
   * Implement this method to create an issue with specific message for the certain check.
   * As expression tree the verified argument is given.
   */
  protected abstract void createIssue(ExpressionTree argument);

  /**
   * Several {@link ArgumentVerifier} and {@link ArgumentMatcher} can be included in the check.
   * This means a single function can be checked for multiple arguments.
   *
   * The order of indicators and verifiers must be observed.
   * On the one hand, an indicator can lead to the termination of the check of a certain function.
   * On the other hand, issues can be created by verifier before an indicator is triggered.
   */
  public void checkArgument(FunctionCallTree tree, String expectedFunctionName, ArgumentMatcher... expectedArgument) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    if (expectedFunctionName.equals(functionName)) {
      for (ArgumentMatcher argumentMatcher : expectedArgument) {
        if ((argumentMatcher.name == null && argumentMatcher.position >= tree.callArguments().size()) || !verifyArgument(tree, argumentMatcher)) {
          return;
        }
      }
    }
  }

  public boolean checkArgumentAbsence(FunctionCallTree tree, String expectedFunctionName, int position) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    List<ExpressionTree> arguments = tree.arguments();
    if (expectedFunctionName.equals(functionName) && position >= arguments.size()) {
      createIssue(tree);
      return true;
    }
    return false;
  }

  private boolean verifyArgument(FunctionCallTree tree, ArgumentMatcher argumentMatcher) {
    Optional<CallArgumentTree> optionalArgument = CheckUtils.argument(tree, argumentMatcher.name, argumentMatcher.position);

    if (optionalArgument.isPresent()) {

      ExpressionTree argument = optionalArgument.get().value();
      ExpressionTree argumentValue = CheckUtils.assignedValue(argument);

      Optional<String> value = nameOf(argumentValue);
      if (value.isPresent()) {
        String quoteLessLowercaseValue = CheckUtils.trimQuotes(value.get()).toLowerCase(Locale.ENGLISH);
        boolean containValues = argumentMatcher.values.contains(quoteLessLowercaseValue);

        if (argumentMatcher instanceof ArgumentVerifier && ((ArgumentVerifier) argumentMatcher).raiseIssueOnMatch == containValues) {
          createIssue(argument);
        }
        return containValues;
    }
  }
    return false;
  }

  private static Optional<String> nameOf(Tree tree) {
    String name;
    if (tree instanceof LiteralTree) {
      name = ((LiteralTree) tree).value();
    } else {
      name = CheckUtils.nameOf(tree);
    }
    return Optional.ofNullable(name);
  }

  protected static class ArgumentMatcher {

    private final int position;

    @Nullable
    private String name;

    private final Set<String> values;

    @Deprecated
    public ArgumentMatcher(int position, String value) {
      this(position, SetUtils.immutableSetOf(value));
    }

    @Deprecated
    public ArgumentMatcher(int position, Set<String> values) {
      this(position, null, values);
    }

    public ArgumentMatcher(int position, @Nullable String name, String value) {
      this(position, name, SetUtils.immutableSetOf(value));
    }

    public ArgumentMatcher(int position, @Nullable String name, Set<String> values) {
      this.position = position;
      this.name = name;
      this.values = values.stream()
        .map(value -> value.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.toSet());
    }

    @VisibleForTesting
    int getPosition() {
      return position;
    }

    @Nullable
    @VisibleForTesting
    String getName() {
      return name;
    }

    @VisibleForTesting
    Set<String> getValues() {
      return values;
    }
  }

  protected static class ArgumentVerifier extends ArgumentMatcher {

    private boolean raiseIssueOnMatch = true;

    @Deprecated
    public ArgumentVerifier(int position, Set<String> values) {
      super(position, values);
    }

    public ArgumentVerifier(int position, String name, Set<String> values) {
      super(position, name, values);
    }

    @Deprecated
    public ArgumentVerifier(int position, String value) {
      this(position, SetUtils.immutableSetOf(value));
    }

    public ArgumentVerifier(int position, String name, String value) {
      this(position, name, SetUtils.immutableSetOf(value));
    }

    public ArgumentVerifier(int position, String value, boolean raiseIssueOnMatch) {
      this(position, SetUtils.immutableSetOf(value));
      this.raiseIssueOnMatch = raiseIssueOnMatch;
    }

    public ArgumentVerifier(int position, String name, String value, boolean raiseIssueOnMatch) {
      this(position, name, SetUtils.immutableSetOf(value));
      this.raiseIssueOnMatch = raiseIssueOnMatch;
    }

    public ArgumentVerifier(int position, Set<String> values, boolean raiseIssueOnMatch) {
      super(position, values);
      this.raiseIssueOnMatch = raiseIssueOnMatch;
    }

    public ArgumentVerifier(int position, String name, Set<String> values, boolean raiseIssueOnMatch) {
      super(position, name, values);
      this.raiseIssueOnMatch = raiseIssueOnMatch;
    }

    @VisibleForTesting
    boolean isRaiseIssueOnMatch() {
      return raiseIssueOnMatch;
    }
  }
}
