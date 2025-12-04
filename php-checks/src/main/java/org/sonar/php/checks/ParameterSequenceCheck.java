/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.HashSet;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.Parameter;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2234")
public class ParameterSequenceCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Parameters to \"%s\" have the same names but not the same order as the method arguments.";
  private static final String SECONDARY_MESSAGE = "Implementation of the parameters sequence.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    // only check method and function calls expect constructors with more than 1 argument
    FunctionSymbol symbol = Symbols.get(tree);
    SeparatedList<CallArgumentTree> arguments = tree.callArguments();
    if (!symbol.isUnknownSymbol() && arguments.size() > 1 && arguments.stream().allMatch(a -> a.name() == null)) {
      checkFunctionCall(tree, symbol);
    }

    super.visitFunctionCall(tree);
  }

  private void checkFunctionCall(FunctionCallTree tree, FunctionSymbol symbol) {
    List<String> parameters = symbol.parameters().stream()
      .map(Parameter::name)
      .toList();

    if (isWrongParameterSequence(tree, parameters)) {
      newIssue(tree, String.format(MESSAGE, symbol.qualifiedName()))
        .secondary(symbol.location(), SECONDARY_MESSAGE);
    }
  }

  private static boolean isWrongParameterSequence(FunctionCallTree call, List<String> parameters) {
    List<String> arguments = CheckUtils.argumentsOfKind(call, Kind.VARIABLE_IDENTIFIER).stream()
      .map(e -> ((VariableIdentifierTree) e).text())
      .toList();

    return arguments.size() == parameters.size() && !arguments.equals(parameters) && new HashSet<>(parameters).equals(new HashSet<>(arguments));
  }
}
