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
package org.sonar.php.compat;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhpFileImplTest {
  private final InputFile inputFile = mock(InputFile.class);

  @Test
  void test() throws Exception {
    URI uri = new URI("uri");

    when(inputFile.contents()).thenReturn("Input file content");
    when(inputFile.filename()).thenReturn("file.php");
    when(inputFile.toString()).thenReturn("to string");
    when(inputFile.uri()).thenReturn(uri);
    when(inputFile.key()).thenReturn("moduleKey:file.php");

    PhpFile phpFile = PhpFileImpl.create(inputFile);

    assertThat(phpFile).isExactlyInstanceOf(PhpFileImpl.class);
    assertThat(phpFile.contents()).isEqualTo("Input file content");
    assertThat(phpFile.filename()).isEqualTo("file.php");
    assertThat(phpFile).hasToString("to string");
    assertThat(phpFile.uri()).isEqualTo(uri);
    assertThat(phpFile.key()).isEqualTo("moduleKey:file.php");
  }

}
