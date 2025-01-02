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
package com.sonar.it.php;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Issues;

import static com.sonar.it.php.Tests.createScanner;
import static org.assertj.core.api.Assertions.assertThat;

class FrameworkDetectionPHPTest {

  @RegisterExtension
  public static final OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;

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
    Tests.provisionProject(key, name, "php", "drupal-profile");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor("drupal_project"))
      .setProjectKey(key)
      .setProjectName(name)
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.php.frameworkDetection", "" + frameworkDetectionEnabled);
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
    return Tests.issuesForComponent(key);
  }
}
