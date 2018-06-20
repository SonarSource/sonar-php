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
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NonPhpProjectTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("js-project");

  private static BuildResult buildResult;

  @BeforeClass
  public static void startServer() {
    orchestrator.resetData();

    SonarScanner build = SonarScanner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs(".");

    buildResult = orchestrator.executeBuild(build);
  }

  @Test
  public void test_execution_of_sensors() {
    assertThat(buildResult.getLogs()).doesNotContain(Tests.PHP_SENSOR_NAME);
    assertThat(buildResult.getLogs()).doesNotContain(Tests.PHP_INI_SENSOR_NAME);
    assertThat(buildResult.getLogs()).contains("1 file indexed");
  }

}
