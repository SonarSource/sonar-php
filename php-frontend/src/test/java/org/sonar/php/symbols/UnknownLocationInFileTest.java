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
import org.sonar.plugins.php.api.visitors.LocationInFile;

import static org.assertj.core.api.Assertions.assertThat;

class UnknownLocationInFileTest {

  @Test
  void unknown() {
    LocationInFile loc = UnknownLocationInFile.UNKNOWN_LOCATION;
    assertThat(loc.filePath()).isEqualTo("[unknown file]");
    assertThat(loc.startLine()).isEqualTo(1);
    assertThat(loc.startLineOffset()).isZero();
    assertThat(loc.endLine()).isEqualTo(1);
    assertThat(loc.endLineOffset()).isEqualTo(1);
  }
}
