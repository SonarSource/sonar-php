/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

  public static final String SONAR_EXCLUSIONS = "sonar.exclusions";

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
    testProject("monica", SONAR_EXCLUSIONS, "tests/**");
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

    SonarScanner build = RulingHelper.prepareScanner(
      projectLocation,
      project,
      "expected/" + project,
      litsDifferencesFile,
      keyValueProperties)
      .setProperty("sonar.import_unknown_files", "true")
      .setProperty("sonar.php.duration.statistics", "true")
      .setProperty("sonar.cpd.exclusions", "**/*")
      .setProperty("sonar.scm.disabled", "true");

    // These exclusions were placed to avoid Windows os raising errors on the exclusions of test monica
    String existingExclusions = build.getProperty(SONAR_EXCLUSIONS);
    String dotFileExclusions = "**/.*, **/.*/**";
    if (existingExclusions != null && !existingExclusions.isEmpty()) {
      build.setProperty(SONAR_EXCLUSIONS, existingExclusions + ", " + dotFileExclusions);
    } else {
      build.setProperty(SONAR_EXCLUSIONS, dotFileExclusions);
    }

    var buildResult = ORCHESTRATOR.executeBuild(build);

    String litsDifferences = new String(Files.readAllBytes(litsDifferencesFile.toPath()));
    assertThat(litsDifferences).isEmpty();

    assertAnalyzerLogs(buildResult.getLogs());
  }
}
