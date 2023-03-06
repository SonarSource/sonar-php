/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.symbols;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationInFileImplTest {
  @Test
  public void getters() {
    LocationInFileImpl loc = new LocationInFileImpl("f1", 1, 2, 3, 4);
    assertThat(loc.filePath()).isEqualTo("f1");
    assertThat(loc.startLine()).isEqualTo(1);
    assertThat(loc.startLineOffset()).isEqualTo(2);
    assertThat(loc.endLine()).isEqualTo(3);
    assertThat(loc.endLineOffset()).isEqualTo(4);
  }

  @Test
  public void test_equals() {
    LocationInFileImpl loc = new LocationInFileImpl("f1", 1, 2, 3, 4);
    assertThat(loc)
      .isEqualTo(loc)
      .isEqualTo(new LocationInFileImpl("f1", 1, 2, 3, 4))
      .isNotEqualTo(new LocationInFileImpl("f2", 1, 2, 3, 4))
      .isNotEqualTo(new LocationInFileImpl("f1", 9, 2, 3, 4))
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 9, 3, 4))
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 2, 9, 4))
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 2, 3, 9))
      .isNotEqualTo("")
      .isNotEqualTo(null);
  }

  @Test
  public void test_hashCode() {
    assertThat(new LocationInFileImpl("f1", 1, 2, 3, 4).hashCode())
      .isEqualTo(new LocationInFileImpl("f1", 1, 2, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f2", 1, 2, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 9, 2, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 9, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 2, 9, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 2, 3, 9).hashCode());
  }

  @Test
  public void test_toString() {
    assertThat(new LocationInFileImpl("f1", 1, 2, 3, 4)).hasToString("LocationInFileImpl{f1, 1, 2, 3, 4}");
  }
}
