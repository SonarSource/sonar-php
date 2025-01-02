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

class ListExpressionTest {

  @Test
  void test() {
    assertThat(Kind.LIST_EXPRESSION)
      .matches("list ()")
      .matches("list (,)")
      .matches("list ($a)")
      .matches("list ($a, $b)")
      .matches("list ($a, ,)")
      .matches("list (list($a), $b)")
      .matches("list (\"a\" => $a, \"b\" => $b)")
      .matches("list (\"a\" => $a, \"b\" => list($b, $c))");
  }
}
