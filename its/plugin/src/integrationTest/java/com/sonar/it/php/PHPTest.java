/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Issues;

import static com.sonar.it.php.Tests.createScanner;
import static com.sonar.it.php.Tests.getComponent;
import static com.sonar.it.php.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

class PHPTest {

  private static final String MULTI_MODULE_PROJECT_KEY = "multimodule-php";
  private static final String EMPTY_FILE_PROJECT_KEY = "empty_file_project_key";
  private static final String PROJECT_WITH_MAIN_AND_TEST_KEY = "project-with-main-and-test";
  private static final String SEVERAL_EXTENSIONS_PROJECT_KEY = "project-with-several-extensions";
  private static final String PROJECT_WITH_VENDOR_KEY = "project-with-vendor";
  private static final String SRC_DIR_NAME = "src";

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;

  /**
   * SONARPLUGINS-1657
   */
  @Test
  void shouldImportSourcesWithUserDefinedFileSuffixes() {
    Tests.provisionProject(SEVERAL_EXTENSIONS_PROJECT_KEY, "Project with several extensions", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor("project-with-several-extensions"))
      .setProperty("sonar.php.file.suffixes", "php,php3,php4,myphp,html");
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(SEVERAL_EXTENSIONS_PROJECT_KEY, "files")).isEqualTo(3);
    assertThat(getMeasureAsInt(getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math2.myphp"), "lines")).isGreaterThan(1);
    assertThat(getComponent(SEVERAL_EXTENSIONS_PROJECT_KEY, getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math3.pgp"))).isNull();
  }

  @Test
  void shouldExcludeVendorDir() {
    Tests.provisionProject(PROJECT_WITH_VENDOR_KEY, "Project with vendor dir", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor("project-with-vendor"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(PROJECT_WITH_VENDOR_KEY, "files")).isEqualTo(1);
  }

  /**
   * SONARPLUGINS-943
   */
  @Test
  void shouldSupportMultimoduleProjects() {
    Tests.provisionProject(MULTI_MODULE_PROJECT_KEY, "Multimodule PHP Project", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor("multimodule"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    String componentKey1 = MULTI_MODULE_PROJECT_KEY + ":module1/src";
    String componentKey2 = MULTI_MODULE_PROJECT_KEY + ":module2/src";

    assertThat(getMeasureAsInt(componentKey1, "files")).isEqualTo(4);
    assertThat(getMeasureAsInt(componentKey2, "files")).isEqualTo(2);
    assertThat(getMeasureAsInt(MULTI_MODULE_PROJECT_KEY, "files")).isEqualTo(4 + 2);
  }

  /**
   * SONARPHP-667
   */
  @Test
  void shouldNotFailOnEmptyFile() {
    Tests.provisionProject(EMPTY_FILE_PROJECT_KEY, "Empty file test project", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectKey(EMPTY_FILE_PROJECT_KEY)
      .setProjectName("Empty file test project")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("empty_file"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(EMPTY_FILE_PROJECT_KEY, "files")).isEqualTo(3);
  }

  @Test
  void shouldNotFailOnDeeplyNestedTrees() {
    Tests.provisionProject("big_concat_key", "Big Concat", "php", "sleep-profile");
    SonarScanner build = createScanner()
      .setProjectKey("big_concat_key")
      .setProjectName("Big Concat")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("big_concat"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    List<Issues.Issue> issues = Tests.issuesForComponent("big_concat_key");
    // The file actually contains two calls to sleep(), but only one is visited due to the depth limit of the visitor.
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).getLine()).isEqualTo(105);
  }

  @Test
  void shouldHandleProjectWithOnlyTestFiles() {
    Tests.provisionProject(PROJECT_WITH_MAIN_AND_TEST_KEY, "project main and test files", "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectKey(PROJECT_WITH_MAIN_AND_TEST_KEY)
      .setProjectName("Test project")
      .setSourceEncoding("UTF-8")
      .setTestDirs("tests")
      .setSourceDirs("")
      .setProjectDir(Tests.projectDirectoryFor("project-with-main-and-test"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    List<Issues.Issue> issues = Tests.issuesForComponent(PROJECT_WITH_MAIN_AND_TEST_KEY);
    assertThat(issues).hasSize(1);
  }

  private static String getResourceKey(String projectKey, String fileName) {
    return projectKey + ":" + SRC_DIR_NAME + "/" + fileName;
  }

}
