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
package org.sonar.php.checks.phpunit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Rule(key = "S2701")
public class BooleanOrNullLiteralInAssertionsCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Use %s instead.";

  private static final Set<String> HANDLED_ASSERTIONS = ImmutableSet.of(
    "assertEquals",
    "assertSame",
    "assertNotEquals",
    "assertNotSame");

  private static final Set<String> INVERSE_ASSERTIONS = ImmutableSet.of("assertNotSame", "assertNotEquals");

  private static final Map<String, String> REPLACEMENT_ASSERTIONS = ImmutableMap.<String, String>builder().
    put("true", "assertTrue()").
    put("false", "assertFalse()").
    put("null", "assertNull()").
    put("!true", "assertNotTrue()").
    put("!false", "assertNotFalse()").
    put("!null", "assertNotNull()").
    build();

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

    context().newIssue(this, functionCallTree,
      String.format(MESSAGE, REPLACEMENT_ASSERTIONS.get(literalValue)));
  }

  private static Optional<LiteralTreeImpl> findLiteralArgument(FunctionCallTree tree) {
    ExpressionTree firstArgument = tree.arguments().get(0);
    ExpressionTree secondArgument = tree.arguments().get(1);

    if (isLiteral(firstArgument)) {
      return Optional.of((LiteralTreeImpl) firstArgument);
    } else if (isLiteral(secondArgument)) {
      return Optional.of((LiteralTreeImpl) secondArgument);
    }

    return Optional.empty();
  }

  private static boolean isLiteral(ExpressionTree tree) {
    return tree.is(Tree.Kind.BOOLEAN_LITERAL, Tree.Kind.NULL_LITERAL);
  }
}
