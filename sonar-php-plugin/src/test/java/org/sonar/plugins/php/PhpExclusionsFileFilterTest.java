/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.internal.MapSettings;

import static org.fest.assertions.Assertions.assertThat;

public class PhpExclusionsFileFilterTest {

  @Test
  public void should_exclude_vendor_dir() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE);
    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());
    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("vendor/my_lib_folder/my_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("sub_module/vendor/submodule_lib.php"))).isFalse();
  }

  @Test
  public void should_exclude_only_php() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE);
    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("vendor/some_lib.ts"))).isTrue();
  }

  @Test
  public void should_include_vendor_when_property_is_empty() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, "");

    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isTrue();
  }

  @Test
  public void should_exclude_using_custom_path_regex() {
    MapSettings settings = new MapSettings();
    settings.setProperty(
      PhpPlugin.PHP_EXCLUSIONS_KEY, PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE + "," + "**/libs/**");

    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
    assertThat(filter.accept(inputFile("libs/some_lib.php"))).isFalse();
  }

  @Test
  public void should_ignore_empty_path_regex() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.PHP_EXCLUSIONS_KEY, "," + PhpPlugin.PHP_EXCLUSIONS_DEFAULT_VALUE + ",");

    PhpExclusionsFileFilter filter = new PhpExclusionsFileFilter(settings.asConfig());

    assertThat(filter.accept(inputFile("some_app.php"))).isTrue();
    assertThat(filter.accept(inputFile("vendor/some_lib.php"))).isFalse();
  }

  private DefaultInputFile inputFile(String file) {
    return new TestInputFileBuilder("test", "test_vendor/" + file)
      .setLanguage(language(file))
      .setContents("<?php foo();")
      .build();
  }

  private static String language(String filename) {
    String[] parts = filename.split("\\.");
    return parts[parts.length - 1];
  }

  
}
