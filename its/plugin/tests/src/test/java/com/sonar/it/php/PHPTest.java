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
import org.junit.ClassRule;
import org.junit.Test;

import static com.sonar.it.php.Tests.getComponent;
import static com.sonar.it.php.Tests.getMeasureAsInt;
import static org.assertj.core.api.Assertions.assertThat;

public class PHPTest {

  private static final String MULTIMODULE_PROJET_KEY = "multimodule-php";
  private static final String EMPTY_FILE_PROJET_KEY = "empty_file_project_key";
  private static final String SEVERAL_EXTENSIONS_PROJECT_KEY = "project-with-several-extensions";
  private static final String SRC_DIR_NAME = "src";

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  /**
   * SONARPLUGINS-1657
   */
  @Test
  public void should_import_sources_with_user_defined_file_suffixes() {
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("project-with-several-extensions"))
      .setProfile("it-profile")
      .setProperty("sonar.php.file.suffixes", "php,php3,php4,myphp,html");
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(SEVERAL_EXTENSIONS_PROJECT_KEY, "files")).isEqualTo(3);
    assertThat(getMeasureAsInt(getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math2.myphp"), "lines")).isGreaterThan(1);
    assertThat(getComponent(SEVERAL_EXTENSIONS_PROJECT_KEY, getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math3.pgp"))).isNull();
  }

  /**
   * SONARPLUGINS-943
   */
  @Test
  public void should_support_multimodule_projects() {
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("multimodule"))
      .setProfile("it-profile");
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(MULTIMODULE_PROJET_KEY + ":module1", "files")).isEqualTo(4);
    assertThat(getMeasureAsInt(MULTIMODULE_PROJET_KEY + ":module2", "files")).isEqualTo(2);
    assertThat(getMeasureAsInt(MULTIMODULE_PROJET_KEY, "files")).isEqualTo(4 + 2);
  }

  /**
   * SONARPHP-667
   */
  @Test
  public void should_not_fail_on_empty_file() {
    SonarScanner build = SonarScanner.create()
      .setProjectKey(EMPTY_FILE_PROJET_KEY)
      .setProjectName("Empty file test project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(Tests.projectDirectoryFor("empty_file"));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);

    assertThat(getMeasureAsInt(EMPTY_FILE_PROJET_KEY, "files")).isEqualTo(3);
  }

  private static String getResourceKey(String projectKey, String fileName) {
    return projectKey + ":" + SRC_DIR_NAME + "/" + fileName;
  }

}
