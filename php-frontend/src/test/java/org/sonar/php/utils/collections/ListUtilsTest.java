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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ListUtilsTest {

  @Test
  public void getLastOnList() {
    List<String> list = List.of("value1", "value2");
    assertThat(ListUtils.getLast(list)).isEqualTo("value2");
  }

  @Test
  public void getLastOnEmptyList() {
    List<String> list = List.of();
    assertThatThrownBy(() -> ListUtils.getLast(list)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void getLastDefaultOnList() {
    List<String> list = List.of("value1", "value2");
    assertThat(ListUtils.getLast(list, "default")).isEqualTo("value2");
  }

  @Test
  public void getLastDefaultOnEmptyList() {
    List<String> list = List.of();
    assertThat(ListUtils.getLast(list, "default")).isEqualTo("default");
  }

  @Test
  public void getOnlyElementOnSingleElementList() {
    List<String> list = List.of("value");
    assertThat(ListUtils.getOnlyElement(list)).isEqualTo("value");
  }

  @Test
  public void getOnlyElementOnMultipleElementList() {
    List<String> list = List.of("value1", "value2");
    assertThatThrownBy(() -> ListUtils.getOnlyElement(list))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Expected list of size 1, but was list of size 2.");
  }

  @Test
  public void getOnlyElementOnEmptyList() {
    List<String> list = List.of();
    assertThatThrownBy(() -> ListUtils.getOnlyElement(list))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Expected list of size 1, but was list of size 0.");
  }

  @Test
  public void testConcatAnyList() {
    List<SomeType> list1 = Arrays.asList(new SomeType("value1"), new SomeType("value2"));
    List<SomeType> list2 = Arrays.asList(new SomeType("value3"), new SomeType("value4"));

    assertThat(ListUtils.concat(list1, list2))
      .containsExactly(new SomeType("value1"), new SomeType("value2"), new SomeType("value3"), new SomeType("value4"));
  }

  @Test
  public void testConcatManyLists() {
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

  @Test
  public void testMergeWithoutDuplicate() {
    List<SomeType> list1 = Arrays.asList(new SomeType("value1"), new SomeType("value2"));
    List<SomeType> list2 = Arrays.asList(new SomeType("value3"), new SomeType("value4"));

    assertThat(ListUtils.merge(list1, list2))
      .hasSize(4)
      .containsExactly(new SomeType("value1"), new SomeType("value2"), new SomeType("value3"), new SomeType("value4"));
  }

  @Test
  public void testMergeWithDuplicate() {
    List<SomeType> list1 = Arrays.asList(new SomeType("value1"), new SomeType("value2"));
    List<SomeType> list2 = Arrays.asList(new SomeType("value2"), new SomeType("value3"));

    assertThat(ListUtils.merge(list1, list2))
      .hasSize(3)
      .containsExactly(new SomeType("value1"), new SomeType("value2"), new SomeType("value3"));
  }

  @Test
  public void testReverseAnyList() {
    List<SomeType> list = Arrays.asList(new SomeType("value1"), new SomeType("value2"));

    assertThat(ListUtils.reverse(list))
      .containsExactly(new SomeType("value2"), new SomeType("value1"));
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
