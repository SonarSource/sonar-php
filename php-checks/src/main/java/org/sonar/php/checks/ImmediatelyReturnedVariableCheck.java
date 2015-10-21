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
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.AssignmentByReferenceTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Rule(
  key = ImmediatelyReturnedVariableCheck.KEY,
  name = "Local variables should not be declared and then immediately returned or thrown",
  priority = Priority.MINOR,
  tags = {Tags.CLUMSY})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class ImmediatelyReturnedVariableCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1488";
  private static final String MESSAGE = "Immediately %s this expression instead of assigning it to the temporary variable \"%s\".";

  private int level = 0;
  private static final Kind[] FUNCTIONS = {
    Kind.FUNCTION_DECLARATION,
    Kind.FUNCTION_EXPRESSION,
    Kind.METHOD_DECLARATION};

  @Override
  public List<Kind> nodesToVisit() {
    Builder<Kind> builder = ImmutableList.builder();
    builder.addAll(Lists.newArrayList(FUNCTIONS));
    builder.addAll(CheckUtils.STATEMENT_CONTAINERS);

    return builder.build();
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(FUNCTIONS)) {
      level++;

    } else if (level > 0) {
      checkStatements(CheckUtils.getStatements(tree));
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (tree.is(FUNCTIONS)) {
      level--;
    }
  }

  private void checkStatements(List<StatementTree> statements) {
    for (int i = 0; i < statements.size() - 1; i++) {

      StatementTree currentStatement = statements.get(i);
      StatementTree nextStatement = statements.get(i + 1);

      List<String> assignedNames = getAssignedVariablesNames(currentStatement);
      String returnedName = getReturnedOrThrownVariableName(nextStatement);

      if (returnedName != null && assignedNames.contains(returnedName)) {
        reportIssue(nextStatement, returnedName, currentStatement);
      }

    }
  }

  private static List<String> getAssignedVariablesNames(StatementTree currentStatement) {
    ExpressionTree variable = null;

    if (currentStatement.is(Kind.EXPRESSION_STATEMENT)) {
      ExpressionTree expression = ((ExpressionStatementTree) currentStatement).expression();

      if (expression instanceof AssignmentExpressionTree) {
        variable = ((AssignmentExpressionTree) expression).variable();

      } else if (expression.is(Kind.ASSIGNMENT_BY_REFERENCE)) {
        variable = ((AssignmentByReferenceTree) expression).variable();
      }
    }

    List<String> names = new ArrayList<>();

    if (variable != null) {

      if (variable.is(Kind.VARIABLE_IDENTIFIER)) {
        names.add(((VariableIdentifierTree) variable).variableExpression().text());
      } else if (variable.is(Kind.LIST_EXPRESSION)) {
        names.addAll(getVariablesFromList((ListExpressionTree) variable));
      }

    }

    return names;
  }

  private static List<String> getVariablesFromList(ListExpressionTree listExpressionTree) {
    List<String> names = new ArrayList<>();
    for (ExpressionTree element : listExpressionTree.elements()) {
      if (element.is(Kind.VARIABLE_IDENTIFIER)) {
        names.add(((VariableIdentifierTree) element).variableExpression().text());
      }
    }
    return names;
  }

  @Nullable
  private static String getReturnedOrThrownVariableName(StatementTree statement) {
    ExpressionTree returnedVariable = null;

    if (statement.is(Kind.RETURN_STATEMENT)) {
      returnedVariable = ((ReturnStatementTree) statement).expression();

    } else if (statement.is(Kind.THROW_STATEMENT)) {
      returnedVariable = ((ThrowStatementTree) statement).expression();
    }

    String returnedName = null;

    if (returnedVariable != null && returnedVariable.is(Kind.VARIABLE_IDENTIFIER)) {
      returnedName = ((VariableIdentifierTree) returnedVariable).variableExpression().text();
    }

    return returnedName;
  }


  private void reportIssue(StatementTree nextStatement, String varName, Tree tree) {
    String message = String.format(MESSAGE, nextStatement.is(Kind.RETURN_STATEMENT) ? "return" : "throw", varName);
    context().newIssue(KEY, message).tree(tree);
  }

}
