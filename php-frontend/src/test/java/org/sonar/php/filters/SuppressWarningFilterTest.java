package org.sonar.php.filters;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SuppressWarningFilterTest extends ParsingTestUtils {

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
    "/*Test comment @SuppressWarnings(\"php:S1234\",\"php:S4567\")*/",
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
    "#[SuppressWarnings(\"php:S1234\")]", // TODO : remove or fix
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
  public void noFilterOutIssue() {
    String code = code("<?php",
      "",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  public void testReset() {
    String code = code("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.scanCompilationUnit("myFile.php", parseSource(code));
    suppressWarningFilter.reset();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  public void filterOutMultipleIssue() {
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
