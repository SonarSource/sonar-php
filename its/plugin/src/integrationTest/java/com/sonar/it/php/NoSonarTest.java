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
package com.sonar.it.php;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Issues.Issue;

import static com.sonar.it.php.Tests.createScanner;
import static org.assertj.core.api.Assertions.assertThat;

class NoSonarTest {

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "nosonar-project";
  private static final String PROJECT_NAME = "NOSONAR Project";

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("nosonar");

  @BeforeAll
  static void startServer() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "nosonar-profile");
    SonarScanner build = createScanner()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".");

    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }

  @Test
  void test() {
    List<Issue> issues = Tests.issuesForComponent(PROJECT_KEY);

    assertThat(Tests.issuesForRule(issues, "php:S1116")).hasSize(1);
    assertThat(Tests.issuesForRule(issues, "php:NoSonar")).hasSize(2);
  }

}
