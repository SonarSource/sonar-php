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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.tree.Tree;

import static org.sonar.php.utils.Assertions.assertThat;

class ArrowFunctionExpressionTest {

  @Test
  void test() {
    assertThat(Tree.Kind.ARROW_FUNCTION_EXPRESSION)
      .matches("fn($x) => $x + $y")
      .matches("fn($x) => fn($y) => $x * $y + $z")
      .matches("fn(array $x) => $x")
      .matches("fn(): int => $x")
      .matches("FN($x = 42) => $x")
      .matches("fn(&$x) => $x")
      .matches("fn&($x) => $x")
      .matches("fn($x, ...$rest) => $rest")
      .matches("static fn() => var_dump($this)")
      .matches("fn() => $x++")
      .matches("fn($str) => preg_match($regex, $str, $matches) && ($matches[1] % 7 == 0)")
      .matches("fn($x) => ($x + $y)")
      .matches("fn($c) => $callable($factory($c), $c)")
      .matches("fn(...$args) => !$f(...$args)")
      .notMatches("fn()");
  }

}
