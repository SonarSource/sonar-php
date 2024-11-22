/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.sonar.it.php.Tests.createScanner;
import static org.assertj.core.api.Assertions.assertThat;

class NonPhpProjectTest {

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "non-php-project";
  private static final String PROJECT_NAME = "Non Php Project";

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("js-project");

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

    buildResult = orchestrator.executeBuild(build);
  }

  @Test
  void testExecutionOfSensors() {
    assertThat(buildResult.getLogs()).doesNotContain(Tests.PHP_SENSOR_NAME);
    assertThat(buildResult.getLogs()).doesNotContain(Tests.PHP_INI_SENSOR_NAME);
    assertThat(buildResult.getLogs()).contains("1 file indexed");
  }

}
