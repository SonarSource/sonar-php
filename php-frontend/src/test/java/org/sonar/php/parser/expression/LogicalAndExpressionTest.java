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
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.sonar.php.utils.Assertions.assertThat;

class LogicalAndExpressionTest {

  @Test
  void test() {
    assertThat(Kind.CONDITIONAL_AND)
      .matches("$a")
      .matches("$a && $a")
      .notMatches("$a and $a");

    assertThat(Kind.ALTERNATIVE_CONDITIONAL_AND)
      .matches("$a AND $a")
      .matches("$a and $a");
  }

}
