/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
import java.nio.charset.Charset;
import org.junit.Test;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompatibilityHelperTest {

  private InputFile inputFile = mock(InputFile.class);
  private SensorContextTester sensorContext = SensorContextTester.create(new File("src/test/resources"));

  private String path = "path/to/file.php";
  private File file = new File(path);


  @Test
  public void test_6_2() throws Exception {
    setSqVersion(6, 2);

    when(inputFile.contents()).thenReturn("Input file content");
    when(inputFile.relativePath()).thenReturn("path/to/file.php");
    when(inputFile.file()).thenReturn(file);

    PhpFile phpFile = CompatibilityHelper.phpFile(inputFile, sensorContext);

    assertThat(phpFile).isExactlyInstanceOf(CompatibleInputFile.class);
    assertThat(phpFile.contents()).isEqualTo("Input file content");
    assertThat(phpFile.relativePath()).isEqualTo(new File("path/to/file.php").toPath());
    assertThat(phpFile.file()).isEqualTo(file);
  }

  @Test
  public void test_6_1() throws Exception {
    setSqVersion(6, 1);

    when(inputFile.charset()).thenReturn(Charset.defaultCharset());
    when(inputFile.path()).thenReturn(new File("src/test/resources/compatibility/file.php").toPath());
    when(inputFile.relativePath()).thenReturn(path);
    when(inputFile.file()).thenReturn(file);

    PhpFile phpFile = CompatibilityHelper.phpFile(inputFile, sensorContext);

    assertThat(phpFile).isExactlyInstanceOf(InputFileV60Compat.class);
    assertThat(phpFile.contents()).isEqualTo("<?php $x = 1;");
    assertThat(phpFile.relativePath()).isEqualTo(file.toPath());
    assertThat(phpFile.file()).isEqualTo(file);
  }

  @Test
  public void test_5_6() throws Exception {
    setSqVersion(5, 6);

    when(inputFile.path()).thenReturn(new File("src/test/resources/compatibility/file.php").toPath());
    when(inputFile.relativePath()).thenReturn(path);
    when(inputFile.file()).thenReturn(file);

    PhpFile phpFile = CompatibilityHelper.phpFile(inputFile, sensorContext);

    assertThat(phpFile).isExactlyInstanceOf(InputFileV56Compat.class);
    assertThat(phpFile.contents()).isEqualTo("<?php $x = 1;");
    assertThat(phpFile.relativePath()).isEqualTo(file.toPath());
    assertThat(phpFile.file()).isEqualTo(file);
  }

  private void setSqVersion(int major, int minor) {
    sensorContext.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(major, minor), SonarQubeSide.SCANNER));
  }
}
