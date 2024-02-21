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
package org.sonar.plugins.php.reports;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalReportFileHandlerTest {

  private static final Path PROJECT_DIR = Paths.get("src", "test", "resources", "extern-file-handler");
  private static final String MODULE_KEY = "ExternalReportFileHandler";

  private ExternalReportFileHandler fileHandler;

  private SensorContextTester context;

  @BeforeEach
  public void setup() {
    context = SensorContextTester.create(PROJECT_DIR);
    addInputFiles("index.php", "subfolder/index.php");
    fileHandler = ExternalReportFileHandler.create(context);
  }

  @Test
  void shouldReturnRelativePathWhenAllFilesAreKnown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "index.php"))).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  void shouldReturnRelativePathWhenFirstFileIsInSubfolder() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "index.php"))).isEqualTo("index.php");
  }

  @Test
  void shouldNotReturnRelativeWhenFilesAreUnknown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "unknown.php"))).isEqualTo(path("foo", "bar", "extern-file-handler", "unknown.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "subfolder", "unknown.php")))
      .isEqualTo(path("foo", "bar", "extern-file-handler", "subfolder", "unknown.php"));
  }

  @Test
  void shouldReturnRelativeWhenFirstFileIsUnknown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "unknown.php"))).isEqualTo(path("foo", "bar", "extern-file-handler", "unknown.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "index.php"))).isEqualTo("index.php");
  }

  @Test
  void shouldReturnRelativePathWhenAlreadyRelative() {
    assertThat(fileHandler.relativePath("index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  void test() {
    assertThat(fileHandler.relativePath(path("unknown", "index.php"))).isEqualTo(path("index.php"));
    assertThat(fileHandler.relativePath("index.php")).isEqualTo("index.php");
  }

  @Test
  void shouldReturnRelativePathForSecondFileWhenUnknown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "index.php"))).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "subfolder", "unknown.php")))
      .isEqualTo(path("foo", "bar", "extern-file-handler", "subfolder", "unknown.php"));
  }

  @Test
  void shouldReturnRelativePathForUnixPath() {
    assertThat(fileHandler.relativePath("/foo/bar/FileHandler/index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath("/foo/bar/FileHandler/subfolder/index.php")).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  void shouldReturnRelativePathForFqnWindowsPath() {
    assertThat(fileHandler.relativePath("C:\\foo\\bar\\FileHandler\\index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath("C:\\foo\\bar\\FileHandler\\subfolder\\index.php")).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  void shouldReturnRelativePathForRelativeWindowsPath() {
    assertThat(fileHandler.relativePath("index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath("subfolder\\index.php")).isEqualTo("subfolder\\index.php");
  }

  @Test
  void shouldReturnHandleShorterPath() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "extern-file-handler", "index.php"))).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("bar", "index.php"))).isEqualTo(path("bar", "index.php"));
  }

  private void addInputFiles(String... paths) {
    Arrays.stream(paths).forEach(path -> context.fileSystem().add(TestInputFileBuilder.create(MODULE_KEY, path).build()));
  }

  private static String path(String first, String... more) {
    return Path.of(first, more).toString();
  }

}
