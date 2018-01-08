/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks.phpini;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.phpini.PhpIniBoolean.OFF;
import static org.sonar.php.checks.phpini.PhpIniBoolean.ON;

public class PhpIniBooleanTest {

  @Test
  public void variants() throws Exception {
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
  public void case_insensitive() throws Exception {
    assertThat(ON.matchesValue("On")).isTrue();
    assertThat(ON.matchesValue("ON")).isTrue();
    assertThat(ON.matchesValue("yeS")).isTrue();
  }

  @Test
  public void quoted_value() throws Exception {
    assertThat(ON.matchesValue("'on'")).isTrue();
    assertThat(ON.matchesValue("'1'")).isTrue();
    assertThat(ON.matchesValue("\"1\"")).isTrue();
    assertThat(ON.matchesValue("'1")).isFalse();
    assertThat(ON.matchesValue("1'")).isFalse();
    assertThat(ON.matchesValue("'x'")).isFalse();
  }

}
