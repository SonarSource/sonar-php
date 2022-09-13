/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalReportFileHandlerTest {

  private static final Path PROJECT_DIR = Paths.get("src", "test", "resources", "FileHandler");
  private static final String MODULE_KEY = "ExternalReportFileHandler";

  private ExternalReportFileHandler fileHandler;

  private SensorContextTester context;

  @Before
  public void setup() {
    context =  SensorContextTester.create(PROJECT_DIR);
    addInputFiles("index.php", "subfolder/index.php");
    fileHandler = ExternalReportFileHandler.create(context);
  }

  @Test
  public void should_return_relative_path_when_all_files_are_known() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "index.php"))).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  public void should_return_relative_path_when_first_file_is_in_subfolder() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "index.php"))).isEqualTo("index.php");
  }


  @Test
  public void should_not_return_relative_when_files_are_unknown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "unknown.php"))).isEqualTo(path("foo", "bar", "FileHandler", "unknown.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "subfolder", "unknown.php"))).isEqualTo(path("foo", "bar", "FileHandler", "subfolder", "unknown.php"));
  }

  @Test
  public void should_return_relative_when_first_file_is_unknown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "unknown.php"))).isEqualTo(path("foo", "bar", "FileHandler", "unknown.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "index.php"))).isEqualTo("index.php");
  }

  @Test
  public void should_return_relative_path_when_already_relative() {
    assertThat(fileHandler.relativePath("index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("subfolder", "index.php"))).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  public void should_return_relative_path_for_second_file_when_unknown() {
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "index.php"))).isEqualTo("index.php");
    assertThat(fileHandler.relativePath(path("foo", "bar", "FileHandler", "subfolder", "unknown.php"))).isEqualTo(path("foo", "bar", "FileHandler", "subfolder", "unknown.php"));
  }

  @Test
  public void should_return_relative_path_for_unix_path() {
    assertThat(fileHandler.relativePath("/foo/bar/FileHandler/index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath("/foo/bar/FileHandler/subfolder/index.php")).isEqualTo(path("subfolder", "index.php"));
  }

  @Test
  public void should_return_relative_path_for_windows_path() {
    assertThat(fileHandler.relativePath("C:\\foo\\bar\\FileHandler\\index.php")).isEqualTo("index.php");
    assertThat(fileHandler.relativePath("C:\\foo\\bar\\FileHandler\\subfolder\\index.php")).isEqualTo(path("subfolder", "index.php"));
  }


  private void addInputFiles(String... paths) {
    Arrays.stream(paths).forEach(path -> context.fileSystem().add(TestInputFileBuilder.create(MODULE_KEY, path).build()));
  }

  private static String path(String first, String... more) {
    return Path.of(first, more).toString();
  }


}
