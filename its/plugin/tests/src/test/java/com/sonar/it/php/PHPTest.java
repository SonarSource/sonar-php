/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2019 SonarSource SA
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
import org.junit.ClassRule;
import org.junit.Test;

import static com.sonar.it.php.Tests.getComponent;
import static com.sonar.it.php.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

public class PHPTest {

  private static final String MULTI_MODULE_PROJECT_KEY = "multimodule-php";
  private static final String EMPTY_FILE_PROJECT_KEY = "empty_file_project_key";
  private static final String SEVERAL_EXTENSIONS_PROJECT_KEY = "project-with-several-extensions";
  private static final String PROJECT_WITH_VENDOR_KEY = "project-with-vendor";
  private static final String SRC_DIR_NAME = "src";

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  /**
   * SONARPLUGINS-1657
   */
  @Test
  public void should_import_sources_with_user_defined_file_suffixes() {
    Tests.provisionProject(SEVERAL_EXTENSIONS_PROJECT_KEY, "Project with several extensions", "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("project-with-several-extensions"))
      .setProperty("sonar.php.file.suffixes", "php,php3,php4,myphp,html");
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(SEVERAL_EXTENSIONS_PROJECT_KEY, "files")).isEqualTo(3);
    assertThat(getMeasureAsInt(getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math2.myphp"), "lines")).isGreaterThan(1);
    assertThat(getComponent(SEVERAL_EXTENSIONS_PROJECT_KEY, getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math3.pgp"))).isNull();
  }

  @Test
  public void should_exclude_vendor_dir() {
    Tests.provisionProject(PROJECT_WITH_VENDOR_KEY, "Project with vendor dir", "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("project-with-vendor"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(PROJECT_WITH_VENDOR_KEY, "files")).isEqualTo(1);
  }

  /**
   * SONARPLUGINS-943
   */
  @Test
  public void should_support_multimodule_projects() {
    Tests.provisionProject(MULTI_MODULE_PROJECT_KEY, "Multimodule PHP Project", "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("multimodule"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    String componentKey1 = MULTI_MODULE_PROJECT_KEY + ":module1";
    String componentKey2 = MULTI_MODULE_PROJECT_KEY + ":module2";
    if (isGreater75()) {
      // starting 7.6, modules were dropped https://jira.sonarsource.com/browse/MMF-365 and are not considered as a component in SQ API
      componentKey1 += "/src";
      componentKey2 += "/src";
    }

    assertThat(getMeasureAsInt(componentKey1, "files")).isEqualTo(4);
    assertThat(getMeasureAsInt(componentKey2, "files")).isEqualTo(2);
    assertThat(getMeasureAsInt(MULTI_MODULE_PROJECT_KEY, "files")).isEqualTo(4 + 2);
  }

  /**
   * SONARPHP-667
   */
  @Test
  public void should_not_fail_on_empty_file() {
    Tests.provisionProject(EMPTY_FILE_PROJECT_KEY, "Empty file test project", "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectKey(EMPTY_FILE_PROJECT_KEY)
      .setProjectName("Empty file test project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("empty_file"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(EMPTY_FILE_PROJECT_KEY, "files")).isEqualTo(3);
  }

  private static String getResourceKey(String projectKey, String fileName) {
    return projectKey + ":" + SRC_DIR_NAME + "/" + fileName;
  }

  private static boolean isGreater75() {
    return orchestrator.getServer().version().isGreaterThanOrEquals(7, 6);
  }

}
