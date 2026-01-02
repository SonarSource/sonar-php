/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package com.sonar.it.php;

import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.sonar.it.php.Tests.createScanner;
import static com.sonar.it.php.Tests.getAnalysisWarnings;
import static com.sonar.it.php.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

class PHPUnitTest {

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "php-unit";
  private static final String PROJECT_NAME = "PHP Unit";

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("phpunit");

  private static final String SOURCE_DIR = "src";
  private static final String TESTS_DIR = "tests";
  private static final String REPORTS_DIR = "reports";
  private static final String COVERED_FILE = "src/Math.php";
  private static final String UNCOVERED_FILE = "src/Math2.php";

  private static final String TEST_FILE = "tests/MathTest.php";

  private final SonarScanner BUILD = createScanner()
    .setProjectDir(PROJECT_DIR)
    .setProjectKey(PROJECT_KEY)
    .setProjectName(PROJECT_NAME)
    .setSourceDirs(SOURCE_DIR)
    .setTestDirs(TESTS_DIR);

  @BeforeAll
  static void startServer() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "it-profile");
  }

  public void setTestReportPath(String reportPath) {
    BUILD.setProperty("sonar.php.tests.reportPath", reportPath);
  }

  public void setCoverageReportPaths(String reportPaths) {
    BUILD.setProperty("sonar.php.coverage.reportPaths", reportPaths);
  }

  public BuildResult executeBuild() {
    return orchestrator.executeBuild(BUILD);
  }

  @Test
  void testsReportWithAbsoluteUnixFilePaths() {
    setTestReportPath(REPORTS_DIR + "/phpunit.tests.xml");
    BuildResult result = executeBuild();

    assertThat(getProjectMetrics("tests")).isEqualTo(2);
    assertThat(getProjectMetrics("test_failures")).isEqualTo(1);
    assertThat(getProjectMetrics("test_errors")).isZero();

    assertThat(getFileMetrics(TEST_FILE, "tests")).isEqualTo(2);
    assertThat(getFileMetrics(TEST_FILE, "test_failures")).isEqualTo(1);
    assertThat(getFileMetrics(TEST_FILE, "test_errors")).isZero();

    assertThat(result.getLogs()).doesNotContain("Failed to resolve 1 file path(s) in PHPUnit tests");
    assertThat(getAnalysisWarnings(result)).isEmpty();
  }

  @Test
  void testsReportWithUnknownFilePaths() {
    setTestReportPath(REPORTS_DIR + "/phpunit.tests.unknown.xml");
    BuildResult result = executeBuild();

    assertThat(getProjectMetrics("tests")).isNull();
    assertThat(getProjectMetrics("test_failures")).isNull();
    assertThat(getProjectMetrics("test_errors")).isNull();

    assertThat(getFileMetrics(TEST_FILE, "tests")).isNull();
    assertThat(getFileMetrics(TEST_FILE, "test_failures")).isZero();
    assertThat(getFileMetrics(TEST_FILE, "test_errors")).isZero();

    assertThat(result.getLogs()).contains("Failed to resolve 1 file path(s) in PHPUnit tests");
    assertThat(getAnalysisWarnings(result)).hasSize(1);
    assertThat(getAnalysisWarnings(result).get(0)).contains("Failed to resolve 1 file path(s) in PHPUnit tests");
  }

  @Test
  void shouldImportMultipleTestReports() {
    setTestReportPath(REPORTS_DIR + "/phpunit.tests.xml," + REPORTS_DIR + "/phpunit.tests.div.xml");
    BuildResult result = executeBuild();

    assertThat(getProjectMetrics("tests")).isEqualTo(3);
    assertThat(getProjectMetrics("test_failures")).isEqualTo(2);
    assertThat(getProjectMetrics("test_errors")).isZero();

    assertThat(getFileMetrics(TEST_FILE, "tests")).isEqualTo(2);
    assertThat(getFileMetrics(TEST_FILE, "test_failures")).isEqualTo(1);
    assertThat(getFileMetrics(TEST_FILE, "test_errors")).isZero();

    assertThat(result.getLogs()).doesNotContain("Failed to resolve 1 file path(s) in PHPUnit tests");
    assertThat(getAnalysisWarnings(result)).isEmpty();
  }

  @Test
  void shouldImportMultipleTestReportsWhenOneIsInvalid() {
    setTestReportPath(REPORTS_DIR + "/phpunit.tests.xml," + REPORTS_DIR + "/phpunit.tests.unknown.xml");
    BuildResult result = executeBuild();

    assertThat(getProjectMetrics("tests")).isEqualTo(2);
    assertThat(getProjectMetrics("test_failures")).isEqualTo(1);
    assertThat(getProjectMetrics("test_errors")).isZero();

    assertThat(getFileMetrics(TEST_FILE, "tests")).isEqualTo(2);
    assertThat(getFileMetrics(TEST_FILE, "test_failures")).isEqualTo(1);
    assertThat(getFileMetrics(TEST_FILE, "test_errors")).isZero();

    assertThat(result.getLogs()).contains("Failed to resolve 1 file path(s) in PHPUnit tests");
    assertThat(getAnalysisWarnings(result)).hasSize(1);
    assertThat(getAnalysisWarnings(result).get(0)).contains("Failed to resolve 1 file path(s) in PHPUnit tests");
  }

  @Test
  void coverageReportWithAbsoluteUnixFilePaths() {
    setCoverageReportPaths(REPORTS_DIR + "/phpunit.coverage.xml");
    BuildResult result = executeBuild();

    assertThat(getProjectMetrics("conditions_to_cover")).isNull();
    assertThat(getFileMetrics(COVERED_FILE, "conditions_to_cover")).isNull();
    assertThat(getFileMetrics(UNCOVERED_FILE, "conditions_to_cover")).isNull();

    assertThat(getProjectMetrics("uncovered_conditions")).isNull();
    assertThat(getFileMetrics(COVERED_FILE, "uncovered_conditions")).isNull();
    assertThat(getFileMetrics(UNCOVERED_FILE, "uncovered_conditions")).isNull();

    assertThat(getProjectMetrics("lines_to_cover")).isEqualTo(12);
    assertThat(getFileMetrics(COVERED_FILE, "lines_to_cover")).isEqualTo(6);
    assertThat(getFileMetrics(UNCOVERED_FILE, "lines_to_cover")).isEqualTo(6);

    assertThat(getProjectMetrics("uncovered_lines")).isEqualTo(8);
    assertThat(getFileMetrics(COVERED_FILE, "uncovered_lines")).isEqualTo(2);
    assertThat(getFileMetrics(UNCOVERED_FILE, "uncovered_lines")).isEqualTo(6);

    assertThat(result.getLogs()).doesNotContain("Failed to resolve 1 file path(s) in PHPUnit coverage");
    assertThat(getAnalysisWarnings(result)).isEmpty();
  }

  @Test
  void coverageReportWithAbsoluteWindowsFilePaths() {
    setCoverageReportPaths(REPORTS_DIR + "/phpunit.coverage.windows.xml");
    executeBuild();

    assertThat(getFileMetrics(COVERED_FILE, "uncovered_lines")).isEqualTo(2);
  }

  @Test
  void coverageReportWithUnknownFilePaths() {
    setCoverageReportPaths(REPORTS_DIR + "/phpunit.coverage.unknown.xml");
    BuildResult result = executeBuild();

    assertThat(getFileMetrics(COVERED_FILE, "uncovered_lines")).isEqualTo(3);

    assertThat(result.getLogs()).contains("Failed to resolve 1 file path(s) in PHPUnit coverage");
    assertThat(getAnalysisWarnings(result)).hasSize(1);
  }

  @Test
  void coverageReportWithNoFileRecord() {
    setCoverageReportPaths(REPORTS_DIR + "/phpunit.coverage-no-record.xml");
    BuildResult result = executeBuild();

    assertThat(result.getLogs()).contains("Coverage report does not contain any record");
    assertThat(getAnalysisWarnings(result)).hasSize(1);
  }

  private Integer getProjectMetrics(String metricKey) {
    return getMeasureAsInt(PROJECT_KEY, metricKey);
  }

  private Integer getFileMetrics(String file, String metricKey) {
    return getMeasureAsInt(PROJECT_KEY + ":" + file, metricKey);
  }
}
