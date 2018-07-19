/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.nio.file.Paths;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PhpFileImplTest {
  private InputFile inputFile = mock(InputFile.class);

  @Test
  public void test() throws Exception {
    when(inputFile.contents()).thenReturn("Input file content");
    when(inputFile.filename()).thenReturn("file.php");
    when(inputFile.toString()).thenReturn("to string");
    when(inputFile.relativePath()).thenReturn("path/to/file.php");

    PhpFile phpFile = new PhpFileImpl(inputFile);

    assertThat(phpFile).isExactlyInstanceOf(PhpFileImpl.class);
    assertThat(phpFile.contents()).isEqualTo("Input file content");
    assertThat(phpFile.filename()).isEqualTo("file.php");
    assertThat(phpFile.relativePath()).isEqualTo(Paths.get("path/to/file.php"));
    assertThat(phpFile.toString()).isEqualTo("to string");
  }
}
