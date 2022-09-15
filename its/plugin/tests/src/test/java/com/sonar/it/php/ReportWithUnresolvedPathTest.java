/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2022 SonarSource SA
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
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import java.util.function.Predicate;
import org.junit.ClassRule;
import org.junit.Test;

import static com.sonar.it.php.Tests.getAnalysisWarnings;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportWithUnresolvedPathTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "report-with-unresolved-path";
  private static final String PROJECT_NAME = "ReportWithUnresolvedPath";

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("phpunit");

  private static final Predicate<String> WARNING = s -> s.startsWith("WARN:");

  @Test
  public void should_log_a_warning() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey(PROJECT_NAME)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0")
      .setSourceDirs("src")
      .setTestDirs("tests")
      .setProperty("sonar.php.coverage.reportPaths", "reports/phpunit.coverage.unknown.xml");
    BuildResult result = orchestrator.executeBuild(build);

    Predicate<String> coverageWarning = s -> s.contains("Failed to resolve 1 file path(s) in PHPUnit coverage phpunit.coverage.unknown.xml report.");

    assertThat(result.getLogsLines(WARNING).stream().anyMatch(coverageWarning)).isTrue();
    assertThat(getAnalysisWarnings(PROJECT_NAME).stream().anyMatch(coverageWarning)).isTrue();
  }

}
