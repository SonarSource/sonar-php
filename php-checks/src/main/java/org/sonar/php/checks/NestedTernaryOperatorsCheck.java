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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = "S3358")
public class NestedTernaryOperatorsCheck extends PHPVisitorCheck {

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    TernaryVisitor visitor = new TernaryVisitor();
    tree.falseExpression().accept(visitor);
    ExpressionTree trueExpression = tree.trueExpression();
    if (trueExpression != null) {
      trueExpression.accept(visitor);
    }
    tree.condition().accept(visitor);
    if (!visitor.descendantTernaries.isEmpty()) {
      ConditionalExpressionTree first = visitor.descendantTernaries.get(0);
      PreciseIssue issue = context().newIssue(this, first, "Extract this nested ternary operation into an independent statement.");
      visitor.descendantTernaries.stream().skip(1).forEach(ternary -> issue.secondary(ternary, "Other nested ternary"));
      issue.secondary(tree.queryToken(), "Parent ternary operator");
    }
    // skip nested
  }

  private static class TernaryVisitor extends PHPVisitorCheck {

    List<ConditionalExpressionTree> descendantTernaries = new ArrayList<>();

    @Override
    public void visitConditionalExpression(ConditionalExpressionTree tree) {
      descendantTernaries.add(tree);
      // skip nested
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      // skip nested
    }

    @Override
    public void visitAnonymousClass(AnonymousClassTree tree) {
      // skip nested
    }

  }

}
