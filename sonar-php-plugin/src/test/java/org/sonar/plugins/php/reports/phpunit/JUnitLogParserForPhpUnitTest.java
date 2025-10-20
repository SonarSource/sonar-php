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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.reports.phpunit.xml.TestSuites;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class JUnitLogParserForPhpUnitTest {

  private JUnitLogParserForPhpUnit parser;

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @BeforeEach
  void setUp() {
    parser = new JUnitLogParserForPhpUnit();
  }

  @Test
  void shouldGenerateEmptyTestSuites() throws Exception {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-with-empty-testsuites.xml"));
    assertThat(suites).isEqualTo(new TestSuites(Collections.emptyList()));
  }

  @Test
  void shouldParseTestSuitesWithoutTime() throws Exception {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-junit-report-no-time.xml"));
    assertThat(suites).isNotNull();
    List<TestFileReport> reports = suites.arrangeSuitesIntoTestFileReports();
    assertThat(reports).hasSize(2);
    assertThat(reports.get(0).testDurationMilliseconds()).isZero();
    assertThat(reports.get(1).testDurationMilliseconds()).isZero();
  }

  @Test
  void shouldThrowAnExceptionWhenReportIsInvalid() {
    File report = new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-invalid.xml");
    Throwable throwable = catchThrowable(() -> parser.parse(report));
    assertThat(throwable).isInstanceOf(ParseException.class);
  }

  @Test
  void shouldThrowAnExceptionWhenReportDoesNotExist() {
    File report = new File("target/unexistingFile.xml");
    Throwable throwable = catchThrowable(() -> parser.parse(report));
    assertThat(throwable).isInstanceOf(IOException.class);
  }

  @Test
  void shouldParseComplexNestedSuites() throws Exception {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-junit-report.xml"));
    List<TestFileReport> reportsPerFile = suites.arrangeSuitesIntoTestFileReports();
    assertThat(reportsPerFile).hasSize(8);
    assertThat(reportsPerFile.get(5).getTests()).isEqualTo(3);
    assertThat(logTester.logs())
      .doesNotContain("Test cases must always be descendants of a file-based suite, skipping : testCanBeUsedAsString with data set #0 in App3Test::testCanBeUsedAsString");
  }

  @Test
  void shouldWarnWhenFileAttributeDoesNotExist() throws Exception {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-no-file.xml"));
    assertThat(suites.arrangeSuitesIntoTestFileReports()).isEmpty();
    assertThat(logTester.logs()).contains("Test cases must always be descendants of a file-based suite, skipping : HelloWorldTest.testFoo2 in HelloWorldTest");
  }
}
