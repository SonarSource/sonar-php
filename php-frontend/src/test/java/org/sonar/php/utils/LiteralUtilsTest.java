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
package org.sonar.php.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralUtilsTest {

  @Test
  void decimalValuesTest() {
    assertThat(LiteralUtils.longLiteralValue("100")).isEqualTo(100);
    assertThat(LiteralUtils.longLiteralValue("100_000")).isEqualTo(100000);
    assertThat(LiteralUtils.longLiteralValue("9223372036854775807")).isEqualTo(9223372036854775807L);
  }

  @Test
  void binaryValuesTest() {
    assertThat(LiteralUtils.longLiteralValue("0b101")).isEqualTo(5);
    assertThat(LiteralUtils.longLiteralValue("0b10_1")).isEqualTo(5);

    assertThat(LiteralUtils.longLiteralValue("0B101")).isEqualTo(5);
    assertThat(LiteralUtils.longLiteralValue("0B10_1")).isEqualTo(5);
  }

  @Test
  void hexadecimalValuesTest() {
    assertThat(LiteralUtils.longLiteralValue("0x1F")).isEqualTo(31);
    assertThat(LiteralUtils.longLiteralValue("0x1_F")).isEqualTo(31);

    assertThat(LiteralUtils.longLiteralValue("0X1F")).isEqualTo(31);
    assertThat(LiteralUtils.longLiteralValue("0X1_F")).isEqualTo(31);
  }
}
