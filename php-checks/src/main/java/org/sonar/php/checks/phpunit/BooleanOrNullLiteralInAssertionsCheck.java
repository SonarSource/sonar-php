/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.phpunit;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.php.utils.collections.MapBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S2701")
public class BooleanOrNullLiteralInAssertionsCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Use %s instead.";

  private static final Set<String> HANDLED_ASSERTIONS = Set.of(
    "assertEquals",
    "assertSame",
    "assertNotEquals",
    "assertNotSame");

  private static final Set<String> INVERSE_ASSERTIONS = Set.of("assertNotSame", "assertNotEquals");

  private static final Map<String, String> REPLACEMENT_ASSERTIONS = MapBuilder.<String, String>builder()
    .put("true", "assertTrue()")
    .put("false", "assertFalse()")
    .put("null", "assertNull()")
    .put("!true", "assertNotTrue()")
    .put("!false", "assertNotFalse()")
    .put("!null", "assertNotNull()")
    .build();

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    getAssertion(tree).ifPresent(a -> {
      if (HANDLED_ASSERTIONS.contains(a.name())) {
        verifyAssertion(tree, a.name());
      }
    });
    super.visitFunctionCall(tree);
  }

  private void verifyAssertion(FunctionCallTree tree, String assertionName) {
    findLiteralArgument(tree).ifPresent(l -> suggestAlternative(l, tree, assertionName));
  }

  private void suggestAlternative(LiteralTreeImpl literalTree, FunctionCallTree functionCallTree, String assertionName) {
    String literalValue = literalTree.value().toLowerCase(Locale.ROOT);
    if (INVERSE_ASSERTIONS.contains(assertionName)) {
      literalValue = "!" + literalValue;
    }

    newIssue(functionCallTree, String.format(MESSAGE, REPLACEMENT_ASSERTIONS.get(literalValue)));
  }

  private static Optional<LiteralTreeImpl> findLiteralArgument(FunctionCallTree tree) {
    Optional<CallArgumentTree> expected = CheckUtils.argument(tree, "expected", 0);
    Optional<CallArgumentTree> actual = CheckUtils.argument(tree, "actual", 1);

    if (!expected.isPresent() || !actual.isPresent()) {
      return Optional.empty();
    }

    ExpressionTree expectedValue = expected.get().value();
    ExpressionTree actualValue = actual.get().value();

    if (isLiteral(expectedValue)) {
      return Optional.of((LiteralTreeImpl) expectedValue);
    } else if (isLiteral(actualValue)) {
      return Optional.of((LiteralTreeImpl) actualValue);
    }

    return Optional.empty();
  }

  private static boolean isLiteral(ExpressionTree tree) {
    return tree.is(Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.NULL_LITERAL);
  }
}
