/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.sonar.php.utils.Assertions.assertThat;

class PipeOperatorExpressionTest {

  @Test
  void test() {
    assertThat(Kind.PIPE)
      .matches("$a |> $b |> $c")
      .matches("$a . $b |> $c . $d")
      .matches("$a |> $b == $c")
      .matches("$c == $a |> $b")
      .matches("($a == $b) |> ($c == $d)")
      .matches("$a . ($b |> $c) . $d")
      .matches("'Hello World' |> 'strtoupper'")
      .matches("$a |> fn($x) => $x + 1")
      .matches("$a |> str_shuffle(...)")
      .matches("$a |> function($x) { return $x + 1; }")
      .matches("$a |> someFunction($x, $y, $z)")
      .matches("$a |> new MyClass()->myInstanceMethod(...)");
  }
}
