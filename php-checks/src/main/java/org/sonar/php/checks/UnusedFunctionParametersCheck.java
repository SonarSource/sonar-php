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
package org.sonar.php.checks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.symbols.Symbols;
import org.sonar.php.symbols.Visibility;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.php.utils.SourceBuilder;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UnusedFunctionParametersCheck.KEY)
public class UnusedFunctionParametersCheck extends PHPVisitorCheck {

  public static final String KEY = "S1172";
  private static final String MESSAGE = "Remove the unused function parameter \"%s\".";
  private final Deque<Boolean> hasFuncGetArgsStack = new ArrayDeque<>();
  private List<IdentifierTree> constructorPromotedProperties = new ArrayList<>();

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String callee = SourceBuilder.build(tree.callee()).trim();
    if (callee.equals("func_get_args")) {
      hasFuncGetArgsStack.pop();
      hasFuncGetArgsStack.push(true);
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    hasFuncGetArgsStack.push(false);
    super.visitFunctionDeclaration(tree);
    if (!hasFuncGetArgsStack.pop()) {
      checkParameters(tree);
    }
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    hasFuncGetArgsStack.push(false);
    super.visitFunctionExpression(tree);
    if (!hasFuncGetArgsStack.pop()) {
      checkParameters(tree);
    }
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    hasFuncGetArgsStack.push(false);
    super.visitMethodDeclaration(tree);
    if (!(isExcluded(tree) || hasFuncGetArgsStack.pop())) {
      collectConstructorPromotedProperties(tree);
      checkParameters(tree);
    }
    constructorPromotedProperties.clear();
  }

  private void checkParameters(FunctionTree tree) {
    Scope scope = context().symbolTable().getScopeFor(tree);
    if (!(scope == null || scope.hasUnresolvedCompact())) {
      List<IdentifierTree> unused = new ArrayList<>();

      for (Symbol symbol : scope.getSymbols(Symbol.Kind.PARAMETER)) {
        if (!isExcluded(symbol) && symbol.usages().isEmpty() && !constructorPromotedProperties.contains(symbol.declaration())) {
          unused.add(symbol.declaration());
        }
      }

      for (IdentifierTree unusedParameter : unused) {
        context().newIssue(this, unusedParameter, String.format(MESSAGE, unusedParameter.text()));
      }
    }
  }

  private void collectConstructorPromotedProperties(MethodDeclarationTree tree) {
    if (tree.name().text().equalsIgnoreCase("__construct")) {
      constructorPromotedProperties = tree.parameters().parameters().stream()
        .filter(p -> p.visibility() != null)
        .map(p -> p.variableIdentifier().variableExpression())
        .collect(Collectors.toList());
    }
  }

  /**
   * Exclude methods from the check that is overriding/implementing a method and are not private.
   */
  private static boolean isExcluded(MethodDeclarationTree tree) {
    MethodSymbol methodSymbol = Symbols.get(tree);
    return !tree.body().is(Tree.Kind.BLOCK)
      || !(methodSymbol.isOverriding().isFalse())
      || (methodSymbol.visibility() != Visibility.PRIVATE && methodSymbol.owner().is(ClassSymbol.Kind.ABSTRACT));
  }

  private static boolean isExcluded(Symbol symbol) {
    return symbol.name().chars()
      // skip the leading '$'
      .skip(1)
      .allMatch(c -> '_' == c);
  }
}
