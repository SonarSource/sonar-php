/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class StringTableTest {

  @Test
  void test() {
    StringTable stringTable = new StringTable();
    assertThat(stringTable.getIndex("a0")).isZero();
    assertThat(stringTable.getIndex("a1")).isEqualTo(1);
    assertThat(stringTable.getIndex("a2")).isEqualTo(2);

    assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> stringTable.getString(3));

    assertThat(stringTable.getString(0)).isEqualTo("a0");
    assertThat(stringTable.getString(2)).isEqualTo("a2");
    assertThat(stringTable.getString(1)).isEqualTo("a1");
  }

  @Test
  void testCreateFromList() {
    List<String> list = new ArrayList<>(Arrays.asList("a0", "a1", "a2"));
    StringTable stringTable = new StringTable(list);
    stringTable.getIndex("a3");

    assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> stringTable.getString(4));

    assertThat(stringTable.getString(0)).isEqualTo("a0");
    assertThat(stringTable.getString(2)).isEqualTo("a2");
    assertThat(stringTable.getString(1)).isEqualTo("a1");
    assertThat(stringTable.getString(3)).isEqualTo("a3");
  }
}
