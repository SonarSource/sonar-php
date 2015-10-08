/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.FunctionUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentByReferenceTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Rule(
  key = LocalVariableAndParameterNameCheck.KEY,
  name = "Local variable and function parameter names should comply with a naming convention",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class LocalVariableAndParameterNameCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S117";

  private static final String MESSAGE = "Rename this %s \"%s\" to match the regular expression %s.";

  private static final ImmutableSet<String> SUPERGLOBALS = ImmutableSet.of(
    "$GLOBALS", "$_SERVER", "$_GET", "$_POST", "$_FILES", "$_COOKIE", "$_SESSION", "$_REQUEST", "$_ENV");

  public static final String DEFAULT = "^[a-z][a-zA-Z0-9]*$";
  private Deque<Set<String>> checkedVariables = new ArrayDeque<Set<String>>();
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
    return ImmutableList.<Kind>builder()
      .addAll(FunctionUtils.DECLARATION_KINDS)
      .add(Kind.ASSIGNMENT_BY_REFERENCE)
      .add(
        Kind.ASSIGNMENT,
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
        Kind.CONCATENATION_ASSIGNMENT)
      .build();
  }

  @Override
  public void visitNode(Tree tree) {
    if (FunctionUtils.isFunctionDeclaration(tree)) {
      enterScope();
      checkParameters((FunctionTree) tree);
    } else if (inScope()) {
      checkLocalVariable(tree);
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (FunctionUtils.isFunctionDeclaration(tree)) {
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
    context().newIssue(KEY, String.format(MESSAGE, type, varName, format)).tree(tree);
    setAsCheckedVariable(varName);
  }

  private ExpressionTree getLeftHandExpression(Tree assignmentExpr) {
    ExpressionTree leftExpression;
    if (assignmentExpr.is(Kind.ASSIGNMENT_BY_REFERENCE)) {
      leftExpression = ((AssignmentByReferenceTree) assignmentExpr).variable();
    } else {
      leftExpression = ((AssignmentExpressionTree) assignmentExpr).variable();
    }
    while (leftExpression.is(Kind.ARRAY_ACCESS)) {
      leftExpression = ((ArrayAccessTree) leftExpression).object();
    }
    return leftExpression;
  }

  private boolean isCompliant(String varName) {
    return pattern.matcher(StringUtils.remove(varName, "$")).matches() || isSuperGlobal(varName);
  }

  private boolean isSuperGlobal(String varName) {
    return SUPERGLOBALS.contains(varName);
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
    checkedVariables.push(new HashSet<String>());
  }

  private void leaveScope() {
    checkedVariables.pop();
  }

}
