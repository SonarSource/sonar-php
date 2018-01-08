/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ForLoopCounterChangedCheck.KEY)
public class ForLoopCounterChangedCheck extends PHPVisitorCheck {

  public static final String KEY = "S127";

  private static final String MESSAGE = "Refactor the code to avoid updating the loop counter \"%s\" within the loop body.";

  private static final Kind[] INCREMENT_DECREMENT = {
    Kind.PREFIX_INCREMENT,
    Kind.PREFIX_DECREMENT,
    Kind.POSTFIX_INCREMENT,
    Kind.POSTFIX_DECREMENT};

  private Deque<Set<String>> counterStack = new ArrayDeque<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    counterStack.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitForStatement(ForStatementTree forStatement) {
    visitAll(forStatement.init());
    visitAll(forStatement.condition());
    visitAll(forStatement.update());

    Set<String> newCounters = new HashSet<>();
    if (!counterStack.isEmpty()) {
      newCounters.addAll(counterStack.peek());
    }
    newCounters.addAll(getCounterNames(forStatement));

    counterStack.push(newCounters);
    visitAll(forStatement.statements());
    counterStack.pop();
  }

  private void visitAll(Iterable<? extends Tree> trees) {
    for (Tree tree : trees) {
      tree.accept(this);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    checkVariable(tree.variable());
    super.visitAssignmentExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    checkUnaryExpressionTree(tree);
    super.visitPrefixExpression(tree);
  }

  @Override
  public void visitPostfixExpression(UnaryExpressionTree tree) {
    checkUnaryExpressionTree(tree);
    super.visitPostfixExpression(tree);
  }

  private void checkUnaryExpressionTree(UnaryExpressionTree tree) {
    if (tree.is(INCREMENT_DECREMENT)) {
      checkVariable(tree.expression());
    }
  }

  private void checkVariable(ExpressionTree variable) {
    if (!counterStack.isEmpty()) {
      String variableName = variable.toString();
      if (counterStack.peek().contains(variableName)) {
        context().newIssue(this, variable, String.format(MESSAGE, variableName));
      }
    }
  }

  private static Set<String> getCounterNames(ForStatementTree forStatement) {
    Set<String> counterNames = new HashSet<>();
    for (ExpressionTree initExpression : forStatement.init()) {

      if (initExpression.is(Kind.ASSIGNMENT)) {
        counterNames.add(((AssignmentExpressionTree) initExpression).variable().toString());
      } else if (initExpression.is(INCREMENT_DECREMENT)) {
        counterNames.add(((UnaryExpressionTree) initExpression).expression().toString());
      }

    }
    return counterNames;
  }

}
