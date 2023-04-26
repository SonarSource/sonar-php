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

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SuppressWarningFilterTest extends ParsingTestUtils {

  private PhpFile prepareFile(String path) throws URISyntaxException {
    PhpFile file = spy(PhpFile.class);
    when(file.uri()).thenReturn(new URI(path));
    return file;
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "#[SuppressWarnings(\"php:S1234\")]",
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
    "/* Test comment @SuppressWarnings  (  \"php:S1234\"  )   */",
  })
  void filterOutIssueNextLine(String suppressWarning) throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      suppressWarning,
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
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
  void filterOutIssueCommentOnSameLineButApplyToNextLine(String suppressWarning) throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      "function foo(){} " + suppressWarning,
      "function bar(){} ");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
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
  void filterOutIssueMultipleRule(String suppressWarning) throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      suppressWarning,
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
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
  void filterOutIssueCommentSeparatedByEmptyLine(String suppressWarning) throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      suppressWarning,
      "",
      "function foo(){} ");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 4)).isFalse();
  }

  @Test
  void noFilterOutIssue() throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      "",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  void testReset() throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    suppressWarningFilter.reset();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  void filterOutMultipleIssue() throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}",
      "// @SuppressWarnings(\"php:S4567\")",
      "function bar(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4567", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 5)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4567", 5)).isFalse();
  }

  @Test
  void filterOutOnMultipleFile() throws URISyntaxException {
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();

    PhpFile file1 = prepareFile("myFile1.php");
    String code1 = asCode("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}");
    suppressWarningFilter.analyze(file1, parseSource(code1));

    PhpFile file2 = prepareFile("myFile2.php");
    String code2 = asCode("<?php",
      "#[SuppressWarnings(\"php:S4567\")]",
      "function foo(){}");
    suppressWarningFilter.analyze(file2, parseSource(code2));

    assertThat(suppressWarningFilter.accept("myFile1.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile1.php", "php:S4567", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile2.php", "php:S1234", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile2.php", "php:S4567", 3)).isFalse();
  }
}
