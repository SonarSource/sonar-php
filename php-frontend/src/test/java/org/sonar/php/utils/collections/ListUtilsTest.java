/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ListUtilsTest {

  @Test
  public void test_concat_any_list() {
    List<SomeType> list1 = Arrays.asList(new SomeType("value1"), new SomeType("value2"));
    List<SomeType> list2 = Arrays.asList(new SomeType("value3"), new SomeType("value4"));

    assertThat(ListUtils.concat(list1, list2))
      .containsExactly(new SomeType("value1"), new SomeType("value2"), new SomeType("value3"), new SomeType("value4"));
  }

  @Test
  public void test_concat_many_lists() {
    List<SomeType> list1 = Arrays.asList(new SomeType("value1"), new SomeType("value2"));
    List<SomeType> list2 = Arrays.asList(new SomeType("value3"), new SomeType("value4"));
    List<SomeType> list3 = Arrays.asList(new SomeType("value5"), new SomeType("value6"));
    List<SomeType> list4 = Arrays.asList(new SomeType("value7"), new SomeType("value8"));
    List<SomeType> list5 = Arrays.asList(new SomeType("value9"), new SomeType("value10"));
    List<SomeType> list6 = Arrays.asList(new SomeType("value11"), new SomeType("value12"));
    List<SomeType> list7 = Arrays.asList(new SomeType("value13"), new SomeType("value14"));

    assertThat(ListUtils.concat(list1, list2, list3, list4, list5, list6, list7))
      .containsExactly(
        new SomeType("value1"), new SomeType("value2"),
        new SomeType("value3"), new SomeType("value4"),
        new SomeType("value5"), new SomeType("value6"),
        new SomeType("value7"), new SomeType("value8"),
        new SomeType("value9"), new SomeType("value10"),
        new SomeType("value11"), new SomeType("value12"),
        new SomeType("value13"), new SomeType("value14")
      );
  }

  private static class SomeType {
    final String value;

    private SomeType(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ListUtilsTest.SomeType value1 = (ListUtilsTest.SomeType) o;
      return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }
}
