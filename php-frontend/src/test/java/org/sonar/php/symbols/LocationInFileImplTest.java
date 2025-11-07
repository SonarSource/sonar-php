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
package org.sonar.php.symbols;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationInFileImplTest {
  @Test
  void getters() {
    LocationInFileImpl loc = new LocationInFileImpl("f1", 1, 2, 3, 4);
    assertThat(loc.filePath()).isEqualTo("f1");
    assertThat(loc.startLine()).isEqualTo(1);
    assertThat(loc.startLineOffset()).isEqualTo(2);
    assertThat(loc.endLine()).isEqualTo(3);
    assertThat(loc.endLineOffset()).isEqualTo(4);
  }

  @Test
  void testEquals() {
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
  void testHashCode() {
    assertThat(new LocationInFileImpl("f1", 1, 2, 3, 4).hashCode())
      .isEqualTo(new LocationInFileImpl("f1", 1, 2, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f2", 1, 2, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 9, 2, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 9, 3, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 2, 9, 4).hashCode())
      .isNotEqualTo(new LocationInFileImpl("f1", 1, 2, 3, 9).hashCode());
  }

  @Test
  void testToString() {
    assertThat(new LocationInFileImpl("f1", 1, 2, 3, 4)).hasToString("LocationInFileImpl{f1, 1, 2, 3, 4}");
  }
}
