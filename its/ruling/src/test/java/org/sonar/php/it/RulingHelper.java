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

import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.junit5.OrchestratorExtensionBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class RulingHelper {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  private static final Pattern DEBUG_AND_INFO_LOG_LINE_PATTERN = Pattern.compile("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s(INFO|DEBUG)\\s.*");
  private static final Pattern CODE_LINE_LOG_LINE_PATTERN = Pattern.compile("\\s*\\d+: .*");
  private static final Pattern CODE_POINTER_LOG_LINE_PATTERN = Pattern.compile("\\s*\\^+");

  static OrchestratorExtension getOrchestrator(Edition sonarEdition) {
    OrchestratorBuilder<OrchestratorExtensionBuilder, OrchestratorExtension> builder = OrchestratorExtension.builderEnv()
      .useDefaultAdminCredentialsForBuilds(true)
      .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
      .setEdition(sonarEdition)
      .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-php-plugin/target"), "sonar-php-plugin-*.jar"))
      .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", "0.11.0.2659"));

    if (sonarEdition != Edition.COMMUNITY) {
      builder.activateLicense();
    }

    return builder.build();
  }

  static OrchestratorExtension getOrchestrator() {
    return getOrchestrator(Edition.COMMUNITY);
  }

  static SonarScanner prepareScanner(File path, String projectKey, String expectedIssueLocation, File litsDifferencesFile, String... keyValueProperties) {
    return SonarScanner.create(path, keyValueProperties)
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.lits.dump.old", FileLocation.of("src/test/resources/" + expectedIssueLocation).getFile().getAbsolutePath())
      .setProperty("sonar.lits.dump.new", FileLocation.of("target/actual").getFile().getAbsolutePath())
      .setProperty("sonar.lits.differences", litsDifferencesFile.getAbsolutePath())
      .setProperty("sonar.internal.analysis.failFast", "true")
      .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx2000m");
  }

  public static void assertAnalyzerLogs(String logs) {
    assertThat(logs).contains("Sensor PHP sensor");

    List<String> lines = Arrays.asList(logs.split("[\r\n]+"));

    List<String> unexpectedLogs = lines.stream()
      .filter(line -> !DEBUG_AND_INFO_LOG_LINE_PATTERN.matcher(line).matches())
      .filter(line -> !CODE_LINE_LOG_LINE_PATTERN.matcher(line).matches())
      .filter(line -> !CODE_POINTER_LOG_LINE_PATTERN.matcher(line).matches())
      .map(line -> line.replaceAll("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s", ""))
      .filter(line -> !line.startsWith("WARN  PHPUnit test cases are detected. Make sure to specify test sources via `sonar.test` to get more precise analysis results."))
      .filter(line -> !line.startsWith("WARN  Invalid character encountered in file"))
      .filter(line -> !line.startsWith("ERROR Unable to parse file"))
      .filter(line -> !line.startsWith("ERROR Parse error at line"))
      .toList();

    assertThat(unexpectedLogs)
      .describedAs("There should be no unexpected lines in the analysis logs")
      .isEmpty();
  }
}
