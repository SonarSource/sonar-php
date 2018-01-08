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

import java.io.File;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompatibleInputFileTest {

  private InputFile inputFile = mock(InputFile.class);

  private String path = "path/to/file.php";
  private File file = new File(path);

  @Test
  public void test() throws Exception {
    when(inputFile.contents()).thenReturn("Input file content");
    when(inputFile.relativePath()).thenReturn("path/to/file.php");
    when(inputFile.file()).thenReturn(file);

    PhpFile phpFile = new CompatibleInputFile(inputFile);

    assertThat(phpFile).isExactlyInstanceOf(CompatibleInputFile.class);
    assertThat(phpFile.contents()).isEqualTo("Input file content");
    assertThat(phpFile.relativePath()).isEqualTo(new File("path/to/file.php").toPath());
    assertThat(phpFile.file()).isEqualTo(file);
  }
}
