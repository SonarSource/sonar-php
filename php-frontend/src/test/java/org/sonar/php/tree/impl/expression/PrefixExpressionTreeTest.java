/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void unaryPlus() {
    testPrefixExpr(Kind.UNARY_PLUS, "+");
  }

  @Test
  void unaryMinus() {
    testPrefixExpr(Kind.UNARY_MINUS, "-");
  }

  @Test
  void prefixInc() {
    testPrefixExpr(Kind.PREFIX_INCREMENT, "++");
  }

  @Test
  void prefixDec() {
    testPrefixExpr(Kind.PREFIX_DECREMENT, "--");
  }

  @Test
  void bitwiseComplement() {
    testPrefixExpr(Kind.BITWISE_COMPLEMENT, "~");
  }

  @Test
  void logicalComplement() {
    testPrefixExpr(Kind.LOGICAL_COMPLEMENT, "!");
  }

  @Test
  void errorControl() {
    testPrefixExpr(Kind.ERROR_CONTROL, "@");
  }

  private void testPrefixExpr(Kind kind, String operator) {
    UnaryExpressionTree tree = parse(operator + "$a", PHPLexicalGrammar.UNARY_EXPR);

    assertThat(tree.is(kind)).isTrue();
    assertThat(tree.operator().text()).isEqualTo(operator);
    assertThat(expressionToString(tree.expression())).isEqualTo("$a");
  }

}
