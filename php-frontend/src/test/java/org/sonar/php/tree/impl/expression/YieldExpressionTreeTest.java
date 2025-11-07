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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class YieldExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void yieldValue() {
    YieldExpressionTree tree = parse("yield $a", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();

    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNull();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(expressionToString(tree.value())).isEqualTo("$a");
  }

  @Test
  void yieldKeyValue() {
    YieldExpressionTree tree = parse("yield $a=>$b", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();

    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNull();
    assertThat(expressionToString(tree.key())).isEqualTo("$a");
    assertThat(expressionToString(tree.doubleArrowToken())).isEqualTo("=>");
    assertThat(expressionToString(tree.value())).isEqualTo("$b");
  }

  @Test
  void yieldNull() {
    YieldExpressionTree tree = parse("yield", PHPLexicalGrammar.YIELD_SCALAR);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();
    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNull();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(tree.value()).isNull();
  }

  @Test
  void yieldFrom() {
    YieldExpressionTree tree = parse("yield from foo()", Kind.YIELD_EXPRESSION);

    assertThat(tree.is(Kind.YIELD_EXPRESSION)).isTrue();

    assertThat(tree.yieldToken().text()).isEqualTo("yield");
    assertThat(tree.fromToken()).isNotNull();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(expressionToString(tree.value())).isEqualTo("foo()");
  }
}
