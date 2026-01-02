/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static java.util.Objects.requireNonNull;
import static org.sonar.php.utils.collections.ListUtils.getLast;

@Rule(key = "S930")
public class FunctionCallArgumentsNumberCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "\"%s\" expects %d argument%s, but %d %s provided. %s";
  private static final String MESSAGE_FEWER = "Add more arguments or define default values.";
  private static final String MESSAGE_MORE = "Reduce provided arguments or add more parameters.";
  private static final String SECONDARY_MESSAGE = "Function definition.";

  private int argumentCount;

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (tree.callee().is(Tree.Kind.NAMESPACE_NAME) && !requireNonNull(tree.getParent()).is(Tree.Kind.NEW_EXPRESSION)) {
      checkArguments(tree);
    }

    super.visitFunctionCall(tree);
  }

  private void checkArguments(FunctionCallTree fct) {
    NamespaceNameTree callee = (NamespaceNameTree) fct.callee();
    FunctionSymbol symbol = Symbols.get(fct);

    if (!symbol.isUnknownSymbol() && !symbol.hasFuncGetArgs()) {
      argumentCount = fct.callArguments().size();
      List<Parameter> parameters = symbol.parameters();
      if (!hasEllipsisOperator(parameters) && argumentCount > maxArguments(parameters)) {
        addIssue(callee, symbol, MESSAGE_MORE, maxArguments(parameters));
      } else if (!hasSpreadArgument(fct) && argumentCount < minArguments(parameters)) {
        addIssue(callee, symbol, MESSAGE_FEWER, minArguments(parameters));
      }
    }
  }

  private void addIssue(NamespaceNameTree callee, FunctionSymbol symbol, String messageAddition, int expectedArguments) {
    String expectedWord = expectedArguments == 1 ? "" : "s";
    String actualWord = argumentCount == 1 ? "was" : "were";
    newIssue(callee, String.format(MESSAGE, callee.fullName(), expectedArguments, expectedWord, argumentCount, actualWord, messageAddition))
      .secondary(symbol.location(), SECONDARY_MESSAGE);
  }

  private static boolean hasEllipsisOperator(List<Parameter> parameters) {
    return !parameters.isEmpty() && getLast(parameters).hasEllipsisOperator();
  }

  private static boolean hasSpreadArgument(FunctionCallTree call) {
    return !CheckUtils.argumentsOfKind(call, Tree.Kind.SPREAD_ARGUMENT).isEmpty();
  }

  private static int minArguments(List<Parameter> parameters) {
    return (int) parameters.stream()
      .filter(p -> !p.hasDefault())
      .filter(p -> !p.hasEllipsisOperator())
      .count();
  }

  private static int maxArguments(List<Parameter> parameters) {
    return parameters.size();
  }
}
