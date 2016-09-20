/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = UnusedFunctionParametersCheck.KEY,
  name = "Unused function parameters should be removed",
  priority = Priority.MAJOR,
  tags = {Tags.UNUSED, Tags.MISRA})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class UnusedFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S1172";
  private static final String MESSAGE = "Remove the unused function parameter%s \"%s\".";

  private Deque<Boolean> mayOverrideStack = new ArrayDeque<>();

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    mayOverrideStack.addLast(mayOverride(tree));

    super.visitClassDeclaration(tree);

    mayOverrideStack.removeLast();
  }

  private static boolean mayOverride(ClassTree tree) {
    return tree.superClass() != null || tree.implementsToken() != null;
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    mayOverrideStack.addLast(mayOverride(tree));

    super.visitAnonymousClass(tree);

    mayOverrideStack.removeLast();
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    checkParameters(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    checkParameters(tree);
    super.visitFunctionExpression(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isExcluded(tree)) {
      checkParameters(tree);
    }
    super.visitMethodDeclaration(tree);
  }

  private void checkParameters(FunctionTree tree) {
    Scope scope = context().symbolTable().getScopeFor(tree);
    if (scope != null) {
      List<String> unused = new ArrayList<>();

      for (Symbol symbol : scope.getSymbols(Symbol.Kind.PARAMETER)) {
        if (symbol.usages().isEmpty()) {
          unused.add(symbol.name());
        }
      }

      if (!unused.isEmpty()) {
        String message = String.format(MESSAGE, unused.size() == 1 ? "" : "s", StringUtils.join(unused, ", "));
        context().newIssue(this, message).tree(tree);
      }
    }
  }

  public boolean isExcluded(MethodDeclarationTree tree) {
    return (mayOverrideStack.getLast() && !CheckUtils.hasModifier(tree.modifiers(), "private"))
      || !tree.body().is(Tree.Kind.BLOCK)
      || CheckUtils.isOverriding(tree);
  }

}
