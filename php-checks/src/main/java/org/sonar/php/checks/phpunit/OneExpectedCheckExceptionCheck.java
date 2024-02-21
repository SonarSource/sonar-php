/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.phpunit;

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.plugins.php.api.tree.Tree.Kind.CLASS_MEMBER_ACCESS;
import static org.sonar.plugins.php.api.tree.Tree.Kind.OBJECT_MEMBER_ACCESS;

@Rule(key = "S5783")
public class OneExpectedCheckExceptionCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Refactor the body of this try/catch to have only one invocation throwing an exception.";

  private final Set<FunctionCallTree> functionCallCount = new HashSet<>();
  private final TryBlockVisitor tryBlockVisitor = new TryBlockVisitor();

  @Override
  public void visitTryStatement(TryStatementTree tree) {
    if (!isPhpUnitTestMethod()) {
      return;
    }

    tree.block().accept(tryBlockVisitor);
    checkFunctionCallCount(tree);

    super.visitTryStatement(tree);
  }

  private void checkFunctionCallCount(TryStatementTree tree) {
    if (functionCallCount.size() > 1) {
      PreciseIssue issue = newIssue(tree.tryToken(), MESSAGE);
      functionCallCount.forEach(call -> addSecondaryLocations(call.callee(), issue));
    }
    functionCallCount.clear();
  }

  private static void addSecondaryLocations(ExpressionTree callee, PreciseIssue issue) {
    Tree tree = callee;
    if (callee.is(OBJECT_MEMBER_ACCESS) || callee.is(CLASS_MEMBER_ACCESS)) {
      tree = ((MemberAccessTree) callee).member();
    }
    issue.secondary(tree, null);
  }

  private class TryBlockVisitor extends PhpUnitCheck {
    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (!isFail(tree)) {
        if (!isAssertion(tree)) {
          functionCallCount.add(tree);
        }

        super.visitFunctionCall(tree);
      }
    }
  }
}
