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
package org.sonar.php.checks.phpini;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.phpini.PhpIniBoolean.OFF;
import static org.sonar.php.checks.phpini.PhpIniBoolean.ON;

class PhpIniBooleanTest {

  @Test
  void variants() throws Exception {
    assertThat(ON.matchesValue("1")).isTrue();
    assertThat(ON.matchesValue("on")).isTrue();
    assertThat(ON.matchesValue("yes")).isTrue();
    assertThat(ON.matchesValue("true")).isTrue();
    assertThat(ON.matchesValue("x")).isFalse();

    assertThat(OFF.matchesValue("0")).isTrue();
    assertThat(OFF.matchesValue("off")).isTrue();
    assertThat(OFF.matchesValue("no")).isTrue();
    assertThat(OFF.matchesValue("false")).isTrue();
    assertThat(OFF.matchesValue("x")).isFalse();
  }

  @Test
  void caseInsensitive() throws Exception {
    assertThat(ON.matchesValue("On")).isTrue();
    assertThat(ON.matchesValue("ON")).isTrue();
    assertThat(ON.matchesValue("yeS")).isTrue();
  }

  @Test
  void quotedValue() throws Exception {
    assertThat(ON.matchesValue("'on'")).isTrue();
    assertThat(ON.matchesValue("'1'")).isTrue();
    assertThat(ON.matchesValue("\"1\"")).isTrue();
    assertThat(ON.matchesValue("'1")).isFalse();
    assertThat(ON.matchesValue("1'")).isFalse();
    assertThat(ON.matchesValue("'x'")).isFalse();
  }

}
