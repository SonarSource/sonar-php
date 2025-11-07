/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.utils.collections;

import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MapBuilderTest {

  @Test
  void testReturnsUnmodifiableMap() {
    MapBuilder<String, String> builder = MapBuilder.builder();
    Map<String, String> map = builder.build();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> map.put("key", "value"));
  }

  @Test
  void testConstructStringsMap() {
    Map<String, String> map = MapBuilder.<String, String>builder()
      .put("key1", "value1")
      .put("key2", "value2")
      .build();

    assertThat(map)
      .hasSize(2)
      .containsEntry("key1", "value1")
      .containsEntry("key2", "value2");
  }

  @Test
  void testConstructAnyMap() {
    Map<Key, Value> map = MapBuilder.<Key, Value>builder()
      .put(new Key(1), new Value("value1"))
      .put(new Key(2), new Value("value2"))
      .build();

    assertThat(map)
      .hasSize(2)
      .containsEntry(new Key(1), new Value("value1"))
      .containsEntry(new Key(2), new Value("value2"));
  }

  private static class Key {
    final int id;

    private Key(int id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Key key = (Key) o;
      return id == key.id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }

  private static class Value {
    final String value;

    private Value(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Value value1 = (Value) o;
      return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }
}
