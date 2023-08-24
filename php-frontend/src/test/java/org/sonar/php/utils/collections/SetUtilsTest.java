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
package org.sonar.php.utils.collections;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SetUtilsTest {

  @Test
  void testConcatAnySet() {
    Set<SomeType> set1 = Set.of(new SomeType("value1"), new SomeType("value2"));
    Set<SomeType> set2 = Set.of(new SomeType("value3"), new SomeType("value4"));

    assertThat(SetUtils.concat(set1, set2))
      .containsExactlyInAnyOrder(new SomeType("value1"), new SomeType("value2"),
        new SomeType("value3"), new SomeType("value4"));
  }

  @Test
  void testConcatManySets() {
    Set<SomeType> set1 = Set.of(new SomeType("value1"), new SomeType("value2"));
    Set<SomeType> set2 = Set.of(new SomeType("value3"), new SomeType("value4"));
    Set<SomeType> set3 = Set.of(new SomeType("value5"), new SomeType("value6"));
    Set<SomeType> set4 = Set.of(new SomeType("value7"), new SomeType("value8"));
    Set<SomeType> set5 = Set.of(new SomeType("value9"), new SomeType("value10"));
    Set<SomeType> set6 = Set.of(new SomeType("value11"), new SomeType("value12"));
    Set<SomeType> set7 = Set.of(new SomeType("value13"), new SomeType("value14"));

    assertThat(SetUtils.concat(set1, set2, set3, set4, set5, set6, set7))
      .containsExactlyInAnyOrder(
        new SomeType("value1"), new SomeType("value2"),
        new SomeType("value3"), new SomeType("value4"),
        new SomeType("value5"), new SomeType("value6"),
        new SomeType("value7"), new SomeType("value8"),
        new SomeType("value9"), new SomeType("value10"),
        new SomeType("value11"), new SomeType("value12"),
        new SomeType("value13"), new SomeType("value14"));
  }

  @Test
  void testNoDifference() {
    Set<String> set1 = Set.of("A", "B", "C");
    Set<String> set2 = Set.of("A", "B", "C");

    assertThat(SetUtils.difference(set1, set2))
      .isEqualTo(Collections.emptySet());
  }

  @Test
  void testNoDifferenceOnLeft() {
    Set<String> set1 = Set.of("A", "B", "C");
    Set<String> set2 = Set.of("A", "B", "C", "D", "E");

    assertThat(SetUtils.difference(set1, set2))
      .isEqualTo(Collections.emptySet());
  }

  @Test
  void testDifference() {
    Set<String> set1 = Set.of("A", "B", "C");
    Set<String> set2 = Set.of("A", "B", "C", "D", "E");

    assertThat(SetUtils.difference(set2, set1))
      .containsExactlyInAnyOrder("D", "E");
  }

  private static class SomeType {
    final String value;

    private SomeType(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      SomeType value1 = (SomeType) o;
      return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }
}
