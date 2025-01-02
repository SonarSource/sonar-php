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
package org.sonar.php.symbols;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrileanTest {

  @Test
  void isTrue() {
    assertThat(Trilean.TRUE.isTrue()).isTrue();
    assertThat(Trilean.FALSE.isTrue()).isFalse();
    assertThat(Trilean.UNKNOWN.isTrue()).isFalse();
  }

  @Test
  void isFalse() {
    assertThat(Trilean.TRUE.isFalse()).isFalse();
    assertThat(Trilean.FALSE.isFalse()).isTrue();
    assertThat(Trilean.UNKNOWN.isFalse()).isFalse();
  }
}
