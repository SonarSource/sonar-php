/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2018 SonarSource SA
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
package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static com.sonar.it.php.PHPUnitTest.createReportsWithAbsolutePath;
import static com.sonar.it.php.Tests.ORCHESTRATOR;
import static com.sonar.it.php.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

public class PHPUnitLegacyTest {

  @ClassRule
  public static Orchestrator orchestrator = ORCHESTRATOR;

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("phpunit");

  private static final String SOURCE_DIR = "src";
  private static final String TESTS_DIR = "tests";
  private static final String REPORTS_DIR = "reports";

  @BeforeClass
  public static void startServer() throws Exception {
    orchestrator.resetData();

    createReportsWithAbsolutePath();

    SonarScanner build = SonarScanner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs(SOURCE_DIR)
      .setTestDirs(TESTS_DIR)
      .setProperty("sonar.php.coverage.reportPath", REPORTS_DIR + "/.coverage-with-absolute-path.xml")
      .setProperty("sonar.php.coverage.itReportPath", REPORTS_DIR + "/.it-coverage-with-absolute-path.xml")
      .setProperty("sonar.php.coverage.overallReportPath", REPORTS_DIR + "/.overall-coverage-with-absolute-path.xml")
      .setProperty("sonar.php.tests.reportPath", REPORTS_DIR + "/.tests-with-absolute-path.xml");
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }

  @Test
  public void tests() throws Exception {
    assertThat(getProjectMeasureAsInt("tests")).isEqualTo(3);
    assertThat(getProjectMeasureAsInt("test_failures")).isEqualTo(1);
    assertThat(getProjectMeasureAsInt("test_errors")).isEqualTo(0);
  }

  @Test
  public void coverage() throws Exception {
    assertThat(getProjectMeasureAsInt("conditions_to_cover")).isNull();
    assertThat(getProjectMeasureAsInt("uncovered_conditions")).isNull();

    assertThat(getCoveredFileMeasureAsInt("lines_to_cover")).isEqualTo(6);
    assertThat(getCoveredFileMeasureAsInt("uncovered_lines")).isEqualTo(2);
    assertThat(getCoveredFileMeasureAsInt("conditions_to_cover")).isNull();
    assertThat(getCoveredFileMeasureAsInt("uncovered_conditions")).isNull();

    assertThat(getUnCoveredFileMeasureAsInt("conditions_to_cover")).isNull();
    assertThat(getUnCoveredFileMeasureAsInt("uncovered_conditions")).isNull();

    assertThat(getProjectMeasureAsInt("lines_to_cover")).isEqualTo(9);
    assertThat(getProjectMeasureAsInt("uncovered_lines")).isEqualTo(5);

    assertThat(getUnCoveredFileMeasureAsInt("lines_to_cover")).isEqualTo(3);
    assertThat(getUnCoveredFileMeasureAsInt("uncovered_lines")).isEqualTo(3);
  }

  private Integer getProjectMeasureAsInt(String metricKey) {
    return getMeasureAsInt("project", metricKey);
  }

  private Integer getCoveredFileMeasureAsInt(String metricKey) {
    return getMeasureAsInt("project:src/Math.php", metricKey);
  }

  private Integer getUnCoveredFileMeasureAsInt(String metricKey) {
    return getMeasureAsInt("project:src/Math2.php", metricKey);
  }
}
