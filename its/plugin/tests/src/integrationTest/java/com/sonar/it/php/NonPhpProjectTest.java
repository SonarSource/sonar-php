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

import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NonPhpProjectTest extends OrchestratorTest {

  private static final String PROJECT_KEY = "non-php-project";
  private static final String PROJECT_NAME = "Non Php Project";

  private static final File PROJECT_DIR = projectDirectoryFor("js-project");

  private static BuildResult buildResult;

  @BeforeAll
  static void startServer() {
    SonarScanner build = createScanner()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setSourceDirs(".")
      // workaround following a change on SonarQube
      .setProperty("sonar.plugins.downloadOnlyRequired", "false");

    buildResult = ORCHESTRATOR.executeBuild(build);
  }

  @Test
  void testExecutionOfSensors() {
    assertThat(buildResult.getLogs()).doesNotContain(PHP_SENSOR_NAME);
    assertThat(buildResult.getLogs()).doesNotContain(PHP_INI_SENSOR_NAME);
    assertThat(buildResult.getLogs()).contains("1 file indexed");
  }

}
