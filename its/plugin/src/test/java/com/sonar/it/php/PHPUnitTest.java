/*
 * PHP :: Integration Tests
 * Copyright (C) 2011 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.it.php;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class PHPUnitTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File projectDir = new File("projects/phpunit/");

  private static final String SOURCE_DIR = "src";
  private static final String TESTS_DIR = "tests";
  private static final String REPORTS_DIR = "reports";

  @BeforeClass
  public static void sspotartServer() throws Exception {
    orchestrator.resetData();

    createReportsWithAbsolutePath();

    SonarRunner build = SonarRunner.create()
      .setProjectDir(projectDir)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs(SOURCE_DIR)
      .setTestDirs(TESTS_DIR)
      .setProperty("sonar.php.coverage.reportPath", REPORTS_DIR + "/.coverage-with-absolute-path.xml")
      .setProperty("sonar.php.coverage.itReportPath", REPORTS_DIR + "/.it-coverage-with-absolute-path.xml")
      .setProperty("sonar.php.coverage.overallReportPath", REPORTS_DIR + "/.overall-coverage-with-absolute-path.xml")
      .setProperty("sonar.php.tests.reportPath", REPORTS_DIR + "/.tests-with-absolute-path.xml");
    orchestrator.executeBuild(build);
  }

  @Test
  public void tests() throws Exception {
    assertThat(getProjectMeasure("tests").getValue()).isEqualTo(3);
    assertThat(getProjectMeasure("test_failures").getValue()).isEqualTo(1);
    assertThat(getProjectMeasure("test_errors").getValue()).isEqualTo(0);
  }

  @Test
  public void coverage() throws Exception {
    assertThat(getProjectMeasure("lines_to_cover").getValue()).isEqualTo(3);
    assertThat(getProjectMeasure("uncovered_lines").getValue()).isEqualTo(1);
    assertThat(getProjectMeasure("conditions_to_cover")).isNull();
    assertThat(getProjectMeasure("uncovered_conditions")).isNull();
  }

  @Test
  public void it_coverage() throws Exception {
    assertThat(getProjectMeasure("it_lines_to_cover").getValue()).isEqualTo(3);
    assertThat(getProjectMeasure("uncovered_lines").getValue()).isEqualTo(1);
  }

  @Test
  public void overall_coverage() throws Exception {
    assertThat(getProjectMeasure("overall_lines_to_cover").getValue()).isEqualTo(3);
    assertThat(getProjectMeasure("overall_uncovered_lines").getValue()).isEqualTo(1);
  }

  private Measure getProjectMeasure(String metricKey) {
    Resource resource = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("project", metricKey));
    return resource == null ? null : resource.getMeasure(metricKey);
  }

  /**
   * Replace file name with absolute path in test and coverage report.
   * <p/>
   * This hack allow to have this integration test, as only absolute path
   * in report is supported.
   */
  private static void createReportsWithAbsolutePath() throws Exception {
    Files.write(
      Files.toString(new File(projectDir, REPORTS_DIR + "/phpunit.overall.coverage.xml"), Charsets.UTF_8)
        .replace("Math.php", new File(projectDir, SOURCE_DIR + "/Math.php").getAbsolutePath()),
      new File(projectDir, REPORTS_DIR + "/.overall-coverage-with-absolute-path.xml"), Charsets.UTF_8);

    Files.write(
      Files.toString(new File(projectDir, REPORTS_DIR + "/phpunit.it.coverage.xml"), Charsets.UTF_8)
        .replace("Math.php", new File(projectDir, SOURCE_DIR + "/Math.php").getAbsolutePath()),
      new File(projectDir, REPORTS_DIR + "/.it-coverage-with-absolute-path.xml"), Charsets.UTF_8);

    Files.write(
      Files.toString(new File(projectDir, REPORTS_DIR + "/phpunit.coverage.xml"), Charsets.UTF_8)
        .replace("Math.php", new File(projectDir, SOURCE_DIR + "/Math.php").getAbsolutePath()),
      new File(projectDir, REPORTS_DIR + "/.coverage-with-absolute-path.xml"), Charsets.UTF_8);

    Files.write(
      Files.toString(new File(projectDir, REPORTS_DIR + "/phpunit.xml"), Charsets.UTF_8)
        .replace("SomeTest.php", new File(projectDir, TESTS_DIR + "/SomeTest.php").getAbsolutePath()),
      new File(projectDir, REPORTS_DIR + "/.tests-with-absolute-path.xml"), Charsets.UTF_8);
  }

}
