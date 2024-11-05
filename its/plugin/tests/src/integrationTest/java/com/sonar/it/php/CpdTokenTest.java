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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.sonar.it.php.Tests.createScanner;

public class CpdTokenTest {

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT = "php8-features";

  @Test
  void supportPhp8Features() {
    Tests.provisionProject(PROJECT, PROJECT, "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectKey(PROJECT)
      .setProjectName(PROJECT)
      .setProjectDir(Tests.projectDirectoryFor(PROJECT));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }
}
