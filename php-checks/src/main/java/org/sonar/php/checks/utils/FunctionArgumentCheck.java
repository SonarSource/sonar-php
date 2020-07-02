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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

/**
 * This abstract class simplifies the checking of function calls for the specific arguments.
 * {@link ArgumentVerifier} can be used to check whether an argument corresponds to a certain value or a set of values.
 * Based on a flag of the verifier an issue can be created on match or non-match.
 * If arguments should only be checked when other arguments must match certain values,
 * {@link ArgumentIndicator} can be used. They are used as a condition for the verification.
 */
public abstract class FunctionArgumentCheck extends PHPVisitorCheck {

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  /**
   * Implement this method to create an issue with specific message for the certain check.
   * As tree argument the verified argument is given.
   */
  protected abstract void createIssue(ExpressionTree tree);

  /**
   * Several {@link ArgumentVerifier} and {@link ArgumentIndicator} can be included in the check.
   * This means a single function can be checked for multiple arguments.
   *
   * The order of indicators and verifiers must be observed.
   * On the one hand, an indicator can lead to the termination of the check of a certain function.
   * On the other hand, issues can be created by verifier before an indicator is triggered.
   */
  public void checkArgument(FunctionCallTree tree, String expectedFunctionName, ArgumentIndicator... expectedArgument) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    List<ExpressionTree> arguments = tree.arguments();

    if (expectedFunctionName.equals(functionName)) {
      for (ArgumentIndicator argumentIndicator: expectedArgument) {
        if (argumentIndicator.position >= arguments.size() || !verifyArgument(arguments, argumentIndicator)) {
          return;
        }
      }
    }
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  private boolean verifyArgument(List<ExpressionTree> arguments, ArgumentIndicator argumentIndicator) {
    ExpressionTree argument = arguments.get(argumentIndicator.position);
    ExpressionTree argumentValue = getAssignedValue(argument);

    Optional<String> value = nameOf(argumentValue);
    if (value.isPresent()) {
      String quoteLessLowercaseValue = CheckUtils.trimQuotes(value.get()).toLowerCase(Locale.ENGLISH);

      boolean containValues = argumentIndicator.values.contains(quoteLessLowercaseValue);

      if (argumentIndicator instanceof ArgumentVerifier && ((ArgumentVerifier) argumentIndicator).raiseIssueOnMatch == containValues) {
        createIssue(argument);
        return true;
      }

      return containValues;
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
    return name != null ? Optional.of(name) : Optional.empty();
  }

  /**
   * Try to resolve the value of a variable which is passed as argument.
   */
  private ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(value);
      return assignmentExpressionVisitor
        .getUniqueAssignedValue(valueSymbol)
        .orElse(value);
    }
    return value;
  }

  protected static class ArgumentIndicator {

    private final int position;

    private final Set<String> values;

    public ArgumentIndicator(int position, String value) {
      this(position, ImmutableSet.of(value));
    }

    public ArgumentIndicator(int position, Set<String> values) {
      this.position = position;
      this.values = values.stream()
        .map(name -> name.toLowerCase(Locale.ENGLISH))
        .collect(Collectors.toSet());
    }

    @VisibleForTesting
    int getPosition() {
      return position;
    }

    @VisibleForTesting
    Set<String> getValues() {
      return values;
    }
  }

  protected static class ArgumentVerifier extends ArgumentIndicator {

    private boolean raiseIssueOnMatch = true;

    public ArgumentVerifier(int position, Set<String> values) {
      super(position, values);
    }

    public ArgumentVerifier(int position, String value) {
      this(position, ImmutableSet.of(value));
    }

    public ArgumentVerifier(int position, String value, boolean raiseIssueOnMatch) {
      this(position, ImmutableSet.of(value));
      this.raiseIssueOnMatch = raiseIssueOnMatch;
    }

    public ArgumentVerifier(int position, Set<String> values, boolean raiseIssueOnMatch) {
      super(position, values);
      this.raiseIssueOnMatch = raiseIssueOnMatch;
    }

    @VisibleForTesting
    boolean isRaiseIssueOnMatch() {
      return raiseIssueOnMatch;
    }
  }
}
