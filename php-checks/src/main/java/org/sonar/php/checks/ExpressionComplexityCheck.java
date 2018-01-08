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

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = ExpressionComplexityCheck.KEY)
public class ExpressionComplexityCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1067";

  private static final String MESSAGE = "Reduce the number of conditional operators (%s) used in the expression (maximum allowed %s).";

  public static final int DEFAULT = 3;

  private Deque<ComplexExpression> expressions = new ArrayDeque<>();

  @RuleProperty(defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  private static final Kind[] COMPLEXITY_INCREMENTING_KINDS = {
    Kind.CONDITIONAL_EXPRESSION,
    Kind.CONDITIONAL_AND,
    Kind.CONDITIONAL_OR,
    Kind.ALTERNATIVE_CONDITIONAL_AND,
    Kind.ALTERNATIVE_CONDITIONAL_OR
  };

  private static final Kind[] NESTING_KINDS = {
    Kind.COMPILATION_UNIT,
    Kind.FUNCTION_EXPRESSION,
    Kind.FUNCTION_CALL
  };

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.<Kind>builder()
      .add(COMPLEXITY_INCREMENTING_KINDS)
      .add(NESTING_KINDS)
      .build();
  }

  private static class ComplexExpression {

    // root is the highest level of a complex expression which may aggregate other logical operators.
    // We don't want to report issues on parts of the complex expression, only at the root.
    private Tree root;
    private int complexity = 0;

    public void increment(Tree tree) {
      if (root == null) {
        root = tree;
      }
      complexity++;
    }

  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(Kind.COMPILATION_UNIT)) {
      expressions.clear();
    }

    if (tree.is(NESTING_KINDS)) {
      expressions.push(new ComplexExpression());
    } else {
      expressions.peek().increment(tree);
    }
  }

  @Override
  public void leaveNode(Tree tree) {
    if (tree.is(NESTING_KINDS)) {

      expressions.pop();

    } else {

      ComplexExpression currentExpression = expressions.peek();

      if (tree.equals(currentExpression.root)) {
        if (currentExpression.complexity > max) {
          String message = String.format(MESSAGE, currentExpression.complexity, max);
          int cost = currentExpression.complexity - max;
          context().newIssue(this, tree, message).cost(cost);
        }
        expressions.pop();
        expressions.push(new ComplexExpression());
      }

    }
  }

}
