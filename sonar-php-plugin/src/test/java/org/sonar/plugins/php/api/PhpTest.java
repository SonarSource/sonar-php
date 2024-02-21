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
package org.sonar.plugins.php.api;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.php.PhpPlugin;

import static org.assertj.core.api.Assertions.assertThat;

class PhpTest {

  @Test
  void shouldCheckDefaultValidPhpExtensions() {
    Php php = new Php(new MapSettings().asConfig());

    assertThat(php.hasValidSuffixes("goodExtension.php")).isTrue();
    assertThat(php.hasValidSuffixes("goodExtension.php5")).isTrue();
    assertThat(php.hasValidSuffixes("goodExtension.inc")).isTrue();

    assertThat(php.hasValidSuffixes("wrong.extension")).isFalse();
    assertThat(php.hasValidSuffixes("goodExtension.java")).isFalse();
    assertThat(php.hasValidSuffixes("goodExtension.cs")).isFalse();
    assertThat(php.hasValidSuffixes("goodExtension.php7")).isFalse();
  }

  @Test
  void shouldCheckCustomValidPhpExtensions() {
    MapSettings settings = new MapSettings();
    settings.setProperty(PhpPlugin.FILE_SUFFIXES_KEY, " php6  , php7, , ");

    Php php = new Php(settings.asConfig());
    assertThat(php.hasValidSuffixes("goodExtension.php6")).isTrue();
    assertThat(php.hasValidSuffixes("goodExtension.php7")).isTrue();
  }

}
