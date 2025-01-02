/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.fail;
import static org.fest.assertions.Assertions.assertThat;

class PhpExclusionsFileFilterTest {

  @Test
  void shouldExcludeVendorDir() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE);
    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());
    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("vendor/my_lib_folder/my_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("sub_module/vendor/submodule_lib.php"))).isFalse();
  }

  @Test
  void shouldExcludeOnlyPhp() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE);
    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("vendor/some_lib.ts"))).isTrue();
  }

  @Test
  void shouldIncludeVendorWhenPropertyIsEmpty() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, "");

    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isTrue();
  }

  @Test
  void shouldExcludeUsingCustomPathRegex() {
    MapSettings settings = new MapSettings();
    settings.setProperty(
      PhpPlugin.PHP_EXCLUSIONS_KEY, PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE + "," + "**/libs/**");

    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("libs/some_lib.php"))).isFalse();
  }

  @Test
  void shouldIgnoreEmptyPathRegex() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, "," + PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE + ",");

    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
  }

  @Timeout(2)
  void shouldIgnorePhpFilesWithAverageLineLengthOverThreshold() throws IOException {
    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(new MapSettings().asConfig());
    File baseDir = new File("src/test/resources/exclusions");
    SensorContextTester context = SensorContextTester.create(baseDir);
    DefaultInputFile testFile = setupSingleFile(baseDir, context, "excluded.php");
    assertThat(filter.accept(testFile)).isFalse();

    // ignore header comment
    testFile = setupSingleFile(baseDir, context, "included.php");
    assertThat(filter.accept(testFile)).isTrue();

    // first line is not a comment nor a single php tag
    testFile = setupSingleFile(baseDir, context, "excluded2.php");
    assertThat(filter.accept(testFile)).isFalse();

    testFile = setupSingleFile(baseDir, context, "excluded3.php");
    assertThat(filter.accept(testFile)).isFalse();

    testFile = setupSingleFile(baseDir, context, "excluded4.php");
    assertThat(filter.accept(testFile)).isFalse();

    //

    testFile = setupSingleFile(baseDir, context, "unknown.php", "");
    try {
      filter.accept(testFile);
      fail("unknown file should have failed");
    } catch (AnalysisException ae) {
      assertThat(ae).hasMessage("Unable to read file 'unknown.php'");
    }

    testFile = setupSingleFile(baseDir, context, "empty.php");
    assertThat(filter.accept(testFile)).isTrue();
  }

  private static DefaultInputFile setupSingleFile(File baseDir, SensorContextTester context, String fileName) throws IOException {
    String content = Files.readString(Path.of(new File(baseDir, fileName).getPath()));
    return setupSingleFile(baseDir, context, fileName, content);
  }

  private static DefaultInputFile setupSingleFile(File baseDir, SensorContextTester context, String fileName, String content) {
    DefaultInputFile file1 = TestInputFileBuilder.create("moduleKey", baseDir, new File(baseDir, fileName))
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata(content)
      .setLanguage("php")
      .build();
    context.fileSystem().add(file1);
    return file1;
  }

  private DefaultInputFile inputFile(String file) {
    return new TestInputFileBuilder("test", "test_vendor/" + file)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(language(file))
      .setContents("<?php foo();")
      .build();
  }

  private static String language(String filename) {
    String[] parts = filename.split("\\.");
    return parts[parts.length - 1];
  }

}
