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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S4507")
public class CakePhpDebugModeCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";

  private static final String CAKE_WRITE_FUNCTION = "Configure::write".toLowerCase(Locale.ROOT);
  private static final String CAKE_CONFIGURE_FUNCTION = "Configure::config".toLowerCase(Locale.ROOT);

  private static final Map<String, String[]> FUNCTION_AND_PARAM_NAMES = ImmutableMap.of(
    CAKE_WRITE_FUNCTION, new String[] {"config", "value"},
    CAKE_CONFIGURE_FUNCTION, new String[] {"name", "engine"});

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getLowerCaseFunctionName(tree);
    if (functionName.equals(CAKE_WRITE_FUNCTION) || functionName.equals(CAKE_CONFIGURE_FUNCTION)) {
      Optional<CallArgumentTree> firstArgument = CheckUtils.argument(tree, FUNCTION_AND_PARAM_NAMES.get(functionName)[0], 0);
      Optional<CallArgumentTree> secondArgument = CheckUtils.argument(tree, FUNCTION_AND_PARAM_NAMES.get(functionName)[1], 1);
      if (firstArgument.isPresent() && secondArgument.isPresent()) {
        check(tree, firstArgument.get().value(), secondArgument.get().value());
      }
    }

    super.visitFunctionCall(tree);
  }

  private void check(FunctionCallTree tree, ExpressionTree firstArg, ExpressionTree secondArg) {
    if (firstArg.is(Tree.Kind.REGULAR_STRING_LITERAL) && trimQuotes((LiteralTree) firstArg).equals("debug")
      && isTrue(secondArg)) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private boolean isTrue(ExpressionTree tree) {
    if (tree.is(Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.NUMERIC_LITERAL, Tree.Kind.REGULAR_STRING_LITERAL) &&
      CheckUtils.isTrueValue(tree)) {
      return true;
    }
    if (tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol symbol = context().symbolTable().getSymbol(tree);
      Optional<ExpressionTree> uniqueAssignedValue = assignmentExpressionVisitor.getUniqueAssignedValue(symbol);
      if (uniqueAssignedValue.isPresent()) {
        ExpressionTree expressionTree = uniqueAssignedValue.get();
        return CheckUtils.isTrueValue(expressionTree);
      }
    }
    return false;
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    this.assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }
}
