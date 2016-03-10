/*
 * PHP :: Integration Tests
 * Copyright (C) 2011 SonarSource
 * sonarqube@googlegroups.com
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

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarRunner;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;

public class ReportWithUnresolvedPathTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("phpunit");

  @Test
  public void should_log_a_warning() throws Exception {
    orchestrator.resetData();
    SonarRunner build = SonarRunner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs("src")
      .setTestDirs("tests")
      .setProperty("sonar.php.coverage.reportPath", "reports/phpunit.coverage.xml");
    BuildResult result = orchestrator.executeBuild(build);
    String logs = result.getLogs();
    String expected = "WARN.*Could not resolve 1 file paths in phpunit.coverage.xml, first unresolved path: Math\\.php";
    assertThat(Pattern.compile(expected).matcher(logs).find()).isTrue();
  }

}
