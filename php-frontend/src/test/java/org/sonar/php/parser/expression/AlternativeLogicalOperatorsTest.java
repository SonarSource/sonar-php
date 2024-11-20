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

class AlternativeLogicalOperatorsTest {

  @Test
  void testAnd() {
    assertThat(Tree.Kind.ALTERNATIVE_CONDITIONAL_AND)
      .matches("$a = $b and $b")
      .matches("$a = $b and $b")
      .matches("$a = $b and $a = $b");
  }

  @Test
  void testOr() {
    assertThat(Tree.Kind.ALTERNATIVE_CONDITIONAL_OR)
      .matches("$a = $b or $b")
      .matches("$a = $b or $b xor $b")
      .matches("$a = $b or $a = $b");
  }

  @Test
  void testXor() {
    assertThat(Tree.Kind.ALTERNATIVE_CONDITIONAL_XOR)
      .matches("$a = $b xor $b")
      .matches("$a = $b xor $b and $b")
      .matches("$a = $b xor $a = $b");
  }
}
