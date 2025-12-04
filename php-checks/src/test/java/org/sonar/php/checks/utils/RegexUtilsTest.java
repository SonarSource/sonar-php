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
package org.sonar.php.checks.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexUtilsTest {

  @Test
  void shouldCreateOneOrMoreRegexExpression() {
    var regex = RegexUtils.oneOrMore("a", "b");
    assertThat(regex).isEqualTo("(?:ab)++");
  }

  @Test
  void shouldCreateZeroOrMoreRegexExpression() {
    var regex = RegexUtils.zeroOrMore("a", "b");
    assertThat(regex).isEqualTo("(?:ab)*+");
  }

  @Test
  void shouldCreateOptionalRegexExpression() {
    var regex = RegexUtils.optional("a", "b");
    assertThat(regex).isEqualTo("(?:ab)?+");
  }

  @Test
  void shouldCreateFirstOfRegexExpression() {
    var regex = RegexUtils.firstOf("a", "b");
    assertThat(regex).isEqualTo("(?:(?:a)|(?:b))");
  }

  @Test
  void shouldCreateFirstOfRegexExpressionSingleValue() {
    var regex = RegexUtils.firstOf("a");
    assertThat(regex).isEqualTo("(?:(?:a))");
  }
}
