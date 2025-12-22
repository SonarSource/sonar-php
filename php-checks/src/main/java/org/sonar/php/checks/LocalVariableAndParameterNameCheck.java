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
package org.sonar.php.checks;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.utils.collections.ListUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = "S117")
public class LocalVariableAndParameterNameCheck extends PHPSubscriptionCheck {

  private static final String MESSAGE = "Rename this %s \"%s\" to match the regular expression %s.";

  public static final String DEFAULT = "^[a-z_][a-zA-Z0-9_]*$";
  private Deque<Set<String>> checkedVariables = new ArrayDeque<>();
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    defaultValue = DEFAULT)
  String format = DEFAULT;

  @Override
  public void init() {
    pattern = Pattern.compile(format);
  }

  @Override
  public List<Kind> nodesToVisit() {
    return ListUtils.concat(CheckUtils.getFunctionKinds(), Arrays.asList(
      Kind.ASSIGNMENT_BY_REFERENCE,
      Kind.ASSIGNMENT,
      Kind.POWER_ASSIGNMENT,
      Kind.MULTIPLY_ASSIGNMENT,
      Kind.DIVIDE_ASSIGNMENT,
      Kind.REMAINDER_ASSIGNMENT,
      Kind.PLUS_ASSIGNMENT,
      Kind.MINUS_ASSIGNMENT,
      Kind.LEFT_SHIFT_ASSIGNMENT,
      Kind.RIGHT_SHIFT_ASSIGNMENT,
      Kind.AND_ASSIGNMENT,
      Kind.XOR_ASSIGNMENT,
      Kind.OR_ASSIGNMENT,
      Kind.CONCATENATION_ASSIGNMENT));
  }

  @Override
  public void visitNode(Tree tree) {
    if (CheckUtils.isFunction(tree)) {
      enterScope();
      checkParameters((FunctionTree) tree);
    } else if (inScope()) {
      checkLocalVariable(tree);
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (CheckUtils.isFunction(tree)) {
      leaveScope();
    }
  }

  private void checkLocalVariable(Tree tree) {
    ExpressionTree variable = getLeftHandExpression(tree);
    if (variable.is(Kind.VARIABLE_IDENTIFIER)) {
      VariableIdentifierTree variableIdentifier = (VariableIdentifierTree) variable;
      String variableName = variableIdentifier.variableExpression().text();
      if (!isAlreadyChecked(variableName) && !isCompliant(variableName)) {
        reportIssue("local variable", variable, variableName);
      }
    }
  }

  private void checkParameters(FunctionTree functionDec) {
    for (ParameterTree parameter : functionDec.parameters().parameters()) {
      String paramName = parameter.variableIdentifier().variableExpression().text();
      if (!isCompliant(paramName)) {
        reportIssue("parameter", parameter, paramName);
      }
    }
  }

  private void reportIssue(String type, Tree tree, String varName) {
    context().newIssue(this, tree, String.format(MESSAGE, type, varName, format));
    setAsCheckedVariable(varName);
  }

  private static ExpressionTree getLeftHandExpression(Tree assignmentExpr) {
    ExpressionTree leftExpression = ((AssignmentExpressionTree) assignmentExpr).variable();

    while (leftExpression.is(Kind.ARRAY_ACCESS)) {
      leftExpression = ((ArrayAccessTree) leftExpression).object();
    }
    return leftExpression;
  }

  private boolean isCompliant(String varName) {
    return pattern.matcher(varName.replace("$", "")).matches() || isSuperGlobal(varName);
  }

  private static boolean isSuperGlobal(String varName) {
    return CheckUtils.SUPERGLOBALS.contains(varName);
  }

  private void setAsCheckedVariable(String varName) {
    checkedVariables.peek().add(varName);
  }

  private boolean isAlreadyChecked(String varName) {
    return checkedVariables.peek().contains(varName);
  }

  private boolean inScope() {
    return !checkedVariables.isEmpty();
  }

  private void enterScope() {
    checkedVariables.push(new HashSet<>());
  }

  private void leaveScope() {
    checkedVariables.pop();
  }

}
