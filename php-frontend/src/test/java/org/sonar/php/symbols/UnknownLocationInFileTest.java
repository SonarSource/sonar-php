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
