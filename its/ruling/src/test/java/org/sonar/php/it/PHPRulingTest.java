/*
 * SonarQube PHP Plugin
 * Copyright (C) 2014-2017 SonarSource SA
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
package org.sonar.php.it;

import com.google.common.io.Files;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class PHPRulingTest {

  private static final File LITS_DIFFERENCES_FILE = FileLocation.of("target/differences").getFile();

  @ClassRule
  public static Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-php-plugin/target"), "sonar-php-plugin-*.jar"))
    .setOrchestratorProperty("litsVersion", "0.5")
    .addPlugin("lits")
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/profile.xml"))
    .build();

  @Test
  public void test() throws Exception {
    assertTrue(
      "SonarQube 5.1 is the minimum version to generate the issues report, change your orchestrator.properties",
      ORCHESTRATOR.getConfiguration().getSonarVersion().isGreaterThanOrEquals("5.1"));
    ORCHESTRATOR.getServer().provisionProject("project", "project");
    ORCHESTRATOR.getServer().associateProjectToQualityProfile("project", "php", "rules");
    SonarScanner build = SonarScanner.create(FileLocation.of("../sources/src").getFile())
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceDirs(".")
      .setSourceEncoding("UTF-8")
      .setProperty("sonar.import_unknown_files", "true")
      .setProperty("sonar.analysis.mode", "preview")
      .setProperty("sonar.issuesReport.html.enable", "true")
      .setProperty("dump.old", FileLocation.of("src/test/resources/expected").getFile().getAbsolutePath())
      .setProperty("dump.new", FileLocation.of("target/actual").getFile().getAbsolutePath())
      .setProperty("sonar.cpd.skip", "true")
      .setProperty("lits.differences", LITS_DIFFERENCES_FILE.getAbsolutePath());
      build.setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx1000m");
    ORCHESTRATOR.executeBuild(build);

    assertThat(Files.toString(LITS_DIFFERENCES_FILE, StandardCharsets.UTF_8)).isEmpty();
  }

}
