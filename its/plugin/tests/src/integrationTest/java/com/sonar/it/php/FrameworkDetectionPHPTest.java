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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkDetectionPHPTest extends OrchestratorTest {

  @Test
  void shouldNotDetectIssueWhenFrameworkDetectionDisabled() {
    var issues = scanDrupalProject("Drupal project 1", "drupal-project-1", false);
    assertThat(issues).isEmpty();
  }

  @Test
  void shouldDetectIssueWhenFrameworkDetectionEnabled() {
    var issues = scanDrupalProject("Drupal project 2", "drupal-project-2", true);
    assertThat(issues).hasSize(1);
  }

  List<Issues.Issue> scanDrupalProject(String name, String key, boolean frameworkDetectionEnabled) {
    provisionProject(key, name, "php", "drupal-profile");
    SonarScanner build = createScanner()
      .setProjectDir(projectDirectoryFor("drupal_project"))
      .setProjectKey(key)
      .setProjectName(name)
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.php.frameworkDetection", "" + frameworkDetectionEnabled);
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);
    return issuesForComponent(key);
  }
}
