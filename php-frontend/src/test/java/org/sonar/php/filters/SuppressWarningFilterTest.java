/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.filters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SuppressWarningFilterTest extends ParsingTestUtils {

  @ParameterizedTest
  @ValueSource(strings = {
    "#[SuppressWarnings(\"php:S1234\")]",
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
    "/* Test comment @SuppressWarnings  (  \"php:S1234\"  )   */",
  })
  void filterOutIssueNextLine(String suppressWarning) {
    String code = code("<?php",
      suppressWarning,
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4657", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("notMyFile.php", "php:S1234", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 8)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
  })
  void filterOutIssueCommentOnSameLineButApplyToNextLine(String suppressWarning) {
    String code = code("<?php",
      "function foo(){} " + suppressWarning,
      "function bar(){} ");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 2)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "#[SuppressWarnings(\"php:S1234\", \"php:S4567\")]",
    "// @SuppressWarnings(\"php:S1234\", \"php:S4567\")",
    "# @SuppressWarnings(\"php:S1234\", \"php:S4567\")",
    "/* @SuppressWarnings(\"php:S1234\", \"php:S4567\") */",
    "/* Test comment @SuppressWarnings  (  \"php:S1234\", \"php:S4567\"  )   */",
    "/*@SuppressWarnings(\"php:S1234\",\"php:S4567\")*/",
  })
  void filterOutIssueMultipleRule(String suppressWarning) {
    String code = code("<?php",
      suppressWarning,
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4567", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S8888", 3)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // "#[SuppressWarnings(\"php:S1234\")]", // TODO : fix
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
  })
  void filterOutIssueCommentSeparatedByEmptyLine(String suppressWarning) {
    String code = code("<?php",
      suppressWarning,
      "",
      "function foo(){} ");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 4)).isFalse();
  }

  @Test
  void noFilterOutIssue() {
    String code = code("<?php",
      "",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  void testReset() {
    String code = code("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    suppressWarningFilter.reset();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  void filterOutMultipleIssue() {
    String code = code("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}",
      "// @SuppressWarnings(\"php:S4567\")",
      "function bar(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4567", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 5)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4567", 5)).isFalse();
  }
}
