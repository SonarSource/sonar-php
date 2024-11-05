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
package org.sonar.php.it;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarsource.analyzer.commons.ProfileGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.it.RulingHelper.assertAnalyzerLogs;

public class PhpGeneralRulingTest {

  @RegisterExtension
  public static OrchestratorExtension ORCHESTRATOR = RulingHelper.getOrchestrator();

  @BeforeAll
  static void prepareQualityProfile() {
    ProfileGenerator.RulesConfiguration parameters = new ProfileGenerator.RulesConfiguration()
      .add("S103", "maximumLineLength", "140")
      .add("S138", "max", "100")
      .add("S1192", "threshold", "10")
      .add("S1479", "max", "100")
      .add("S1541", "threshold", "10")
      // force start with capital letter
      .add("S1578", "format", "[A-Z][A-Za-z0-9]+.php")
      .add("S2004", "max", "2")
      .add("S2042", "maximumLinesThreshold", "500");
    Set<String> disabledRules = new HashSet<>();
    // platform dependent
    disabledRules.add("S1779");

    String serverUrl = ORCHESTRATOR.getServer().getUrl();
    File profileFile = ProfileGenerator.generateProfile(serverUrl, "php", "php", parameters, disabledRules);
    ORCHESTRATOR.getServer().restoreProfile(FileLocation.of(profileFile));
  }

  @Test
  void testFlysystem() throws Exception {
    testProject("flysystem");
  }

  @Test
  void testMonica() throws Exception {
    // To avoid error: File tests/Unit/Traits/SearchableTest.php can't be indexed twice...
    // the tests directory needs to be excluded
    testProject("monica", "sonar.exclusions", "**/tests/**");
  }

  @Test
  void testPhpCodeSniffer() throws Exception {
    testProject("PHP_CodeSniffer");
  }

  @Test
  void testPhpMailer() throws Exception {
    testProject("PHPMailer");
  }

  @Test
  void testPsysh() throws Exception {
    testProject("psysh");
  }

  @Test
  void testPhpWord() throws Exception {
    testProject("PHPWord");
  }

  @Test
  void testRubixML() throws Exception {
    testProject("RubixML");
  }

  @Test
  void testPhpSpreadsheet() throws Exception {
    testProject("PhpSpreadsheet");
  }

  private void testProject(String project, String... keyValueProperties) throws Exception {
    ORCHESTRATOR.getServer().provisionProject(project, project);
    ORCHESTRATOR.getServer().associateProjectToQualityProfile(project, "php", "rules");
    File litsDifferencesFile = FileLocation.of("build/differences").getFile();
    File projectLocation = FileLocation.of("../sources/src/" + project).getFile();

    SonarScanner build = SonarScanner.create(projectLocation, keyValueProperties)
      .setProjectKey(project)
      .setProjectName(project)
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.lits.dump.old", FileLocation.of("src/integrationTest/resources/expected/" + project).getFile().getAbsolutePath())
      .setProperty("sonar.lits.dump.new", FileLocation.of("target/actual").getFile().getAbsolutePath())
      .setProperty("sonar.lits.differences", litsDifferencesFile.getAbsolutePath())
      .setProperty("sonar.internal.analysis.failFast", "true")
      .setProperty("sonar.import_unknown_files", "true")
      .setProperty("sonar.php.duration.statistics", "true")
      .setProperty("sonar.cpd.exclusions", "**/*")
      .setProperty("sonar.scm.disabled", "true")
      .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx2000m");

    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      // On unix systems, files or directories starting with a dot are hidden, so we don't get them from the scanner
      // In order to have the same behavior on all systems, we exclude them on windows
      build.setProperty("sonar.exclusions", "**/.*, **/.*/**");
    }

    var buildResult = ORCHESTRATOR.executeBuild(build);

    String litsDifferences = new String(Files.readAllBytes(litsDifferencesFile.toPath()));
    assertThat(litsDifferences).isEmpty();

    assertAnalyzerLogs(buildResult.getLogs());
  }
}
