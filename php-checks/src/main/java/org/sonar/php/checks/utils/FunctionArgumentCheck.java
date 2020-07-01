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

public abstract class FunctionArgumentCheck extends PHPVisitorCheck {

  protected abstract void createIssue(ExpressionTree tree);

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  public void checkArgument(FunctionCallTree tree, String expectedFunctionName, ArgumentIndicator... expectedArgument) {
    if (isExpectedFunction(tree, expectedFunctionName)) {
      List<ExpressionTree> arguments = tree.arguments();
      for (ArgumentIndicator argumentIndicator: expectedArgument) {
        if (argumentIndicator.position >= arguments.size() || !verifyArgument(arguments, argumentIndicator)) {
          return;
        }
      }
    }
  }

  public boolean checkArgumentAbsence(FunctionCallTree tree, String expectedFunctionName, int position) {
    return checkArgumentAbsence(tree, expectedFunctionName, position, true);
  }

  public boolean checkArgumentAbsence(FunctionCallTree tree, String expectedFunctionName, int position, boolean raiseIssueOnAbsence) {
    if (isExpectedFunction(tree, expectedFunctionName)) {
      List<ExpressionTree> arguments = tree.arguments();
      if (arguments.size() <= position == raiseIssueOnAbsence) {
        createIssue(tree);
        return true;
      }
    }
    return false;
  }

  private static boolean isExpectedFunction(FunctionCallTree tree, String expectedFunctionName) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);

    return expectedFunctionName.equals(functionName);
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

  protected static Optional<String> nameOf(Tree tree) {
    String name;
    if (tree instanceof LiteralTree) {
      name = ((LiteralTree) tree).value();
    } else {
      name = CheckUtils.nameOf(tree);
    }
    return name != null ? Optional.of(name) : Optional.empty();
  }

  private ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(value);
      return assignmentExpressionVisitor
        .getUniqueAssignedValue(valueSymbol)
        .orElse(value);
    }
    return value;
  }

  public static class ArgumentIndicator {

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
  }

  public static class ArgumentVerifier extends ArgumentIndicator {

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
  }
}
