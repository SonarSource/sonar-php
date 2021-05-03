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
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.AbstractStatementsCheck;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.utils.collections.ListUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ImmediatelyReturnedVariableCheck.KEY)
public class ImmediatelyReturnedVariableCheck extends AbstractStatementsCheck {

  public static final String KEY = "S1488";
  private static final String MESSAGE = "Immediately %s this expression instead of assigning it to the temporary variable \"%s\".";

  private int level = 0;

  @Override
  public List<Kind> nodesToVisit() {
    return ListUtils.concat(CheckUtils.FUNCTION_KINDS, super.nodesToVisit());
  }

  @Override
  public void visitNode(Tree tree) {
    if (CheckUtils.isFunction(tree)) {
      level++;

    } else if (level > 0) {
      checkStatements(getStatements(tree));
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (CheckUtils.isFunction(tree)) {
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
        ExpressionTree variable = ((AssignmentExpressionTree) ((ExpressionStatementTree) currentStatement).expression()).variable();
        reportIssue(nextStatement, returnedName, variable);
      }

    }
  }

  private static List<String> getAssignedVariablesNames(StatementTree currentStatement) {
    ExpressionTree variable = null;
    ExpressionTree value = null;

    if (currentStatement.is(Kind.EXPRESSION_STATEMENT)) {
      ExpressionTree expression = ((ExpressionStatementTree) currentStatement).expression();

      if (expression.is(Kind.ASSIGNMENT, Kind.ASSIGNMENT_BY_REFERENCE)) {
        variable = ((AssignmentExpressionTree) expression).variable();
        value = ((AssignmentExpressionTree) expression).value();
      }
    }

    List<String> names = new ArrayList<>();

    if (variable != null) {

      if (variable.is(Kind.VARIABLE_IDENTIFIER)) {
        names.add(((VariableIdentifierTree) variable).variableExpression().text());
      } else if (variable.is(Kind.LIST_EXPRESSION)) {
        names.addAll(getVariablesFromList((ListExpressionTree) variable));
      }

      // Ignore assigned variables for which the assigned value depends on the variable itself.
      UsedVariablesCollector usedVariablesCollector = new UsedVariablesCollector();
      value.accept(usedVariablesCollector);
      names.removeAll(usedVariablesCollector.usedVariables);
    }

    return names;
  }

  private static List<String> getVariablesFromList(ListExpressionTree listExpressionTree) {
    List<String> names = new ArrayList<>();
    for (Optional<ArrayAssignmentPatternElementTree> element : listExpressionTree.elements()) {
      if (element.isPresent() && element.get().variable().is(Kind.VARIABLE_IDENTIFIER)) {
        names.add(((VariableIdentifierTree) element.get().variable()).variableExpression().text());
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
    context().newIssue(this, tree, message);
  }

  static class UsedVariablesCollector extends PHPVisitorCheck {
    private final Set<String> usedVariables = new HashSet<>();

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      usedVariables.add(tree.variableExpression().text());
      super.visitVariableIdentifier(tree);
    }
  }
}
