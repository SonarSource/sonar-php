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
package org.sonar.php.checks.security;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.argument;
import static org.sonar.php.checks.utils.CheckUtils.lowerCaseFunctionName;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S5876")
public class SessionFixationStrategyCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Create a new session during user authentication to prevent session fixation attacks.";
  private static final String SECURITY_COMPONENT = "security";
  private static final String SENSITIVE_VALUE = "none";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = lowerCaseFunctionName(tree);
    if (("loadfromextension".equals(functionName) && isArgumentEqualsTo(tree, "extension", 0, SECURITY_COMPONENT) && isArgumentSensitiveArray(tree, "values", 1)) ||
      (("extension".equals(functionName) && isArgumentEqualsTo(tree, "namespace", 0, SECURITY_COMPONENT) && isArgumentSensitiveArray(tree, "config", 1)) ||
        ("prependextensionconfig".equals(functionName) && isArgumentEqualsTo(tree, "name", 0, SECURITY_COMPONENT) && isArgumentSensitiveArray(tree, "config", 1)))) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitFunctionCall(tree);
  }

  private static boolean isArgumentEqualsTo(FunctionCallTree functionCallTree, String name, int position, String expected) {
    Optional<CallArgumentTree> argumentTree = argument(functionCallTree, name, position);
    return argumentTree.isPresent() && isLiteralTreeEqualsTo(argumentTree.get().value(), expected);
  }

  private static boolean isLiteralTreeEqualsTo(ExpressionTree tree, String expected) {
    return tree.is(Kind.REGULAR_STRING_LITERAL) && expected.equalsIgnoreCase(trimQuotes(((LiteralTree) tree)));
  }

  private static boolean isArgumentSensitiveArray(FunctionCallTree functionCallTree, String name, int index) {
    return argument(functionCallTree, name, index)
      .map(CallArgumentTree::value)
      .map(CheckUtils::assignedValue)
      .filter(ArrayInitializerTree.class::isInstance)
      .map(ArrayInitializerTree.class::cast)
      .filter(arrayTree -> arrayTree.arrayPairs()
        .stream()
        .anyMatch(SessionFixationStrategyCheck::isArrayPairSensitive))
      .isPresent();
  }

  private static boolean isArrayPairSensitive(ArrayPairTree pair) {
    ExpressionTree key = pair.key();
    return key != null && isLiteralTreeEqualsTo(pair.key(), "session_fixation_strategy") && isLiteralTreeEqualsTo(pair.value(), SENSITIVE_VALUE);
  }
}
