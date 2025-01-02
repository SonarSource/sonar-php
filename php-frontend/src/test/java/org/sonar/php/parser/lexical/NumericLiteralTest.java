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
package org.sonar.php.parser.lexical;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.sonar.php.utils.Assertions.assertThat;

class NumericLiteralTest {

  @Test
  void test() {
    assertThat(Kind.NUMERIC_LITERAL)
      .matches("7E-10")
      .matches("1.2e3")
      .matches("1.234")
      .matches("0b11111111")
      .matches("0B11111111")
      .matches("0x1A")
      .matches("0X1A")
      .matches("0123")
      .matches("1")
      .matches("0_0")
      .matches("299_792_458")
      .matches("0xCAFE_F00D")
      .matches("0b0101_1111")
      .matches("016")
      .matches("0o16")
      .matches("0O16")

      .notMatches("string")
      .notMatches("o1234");
  }
}
