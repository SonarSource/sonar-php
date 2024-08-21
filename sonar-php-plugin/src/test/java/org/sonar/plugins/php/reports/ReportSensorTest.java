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
package org.sonar.plugins.php.reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class ReportSensorTest {
  protected static final SonarRuntime SONAR_RUNTIME = SonarRuntimeImpl.forSonarQube(Version.create(10, 6), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  protected final AnalysisWarningsWrapper analysisWarnings = spy(AnalysisWarningsWrapper.class);

  protected static String language(Path file) {
    String path = file.toString();
    return path.substring(path.lastIndexOf('.') + 1);
  }

  protected static String onlyOneLogElement(List<String> elements) {
    assertThat(elements).hasSize(1);
    return elements.get(0);
  }

  protected static void assertNoErrorWarnDebugLogs(LogTesterJUnit5 logTester) {
    org.assertj.core.api.Assertions.assertThat(logTester.logs(Level.ERROR)).isEmpty();
    org.assertj.core.api.Assertions.assertThat(logTester.logs(Level.WARN)).isEmpty();
    org.assertj.core.api.Assertions.assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  protected List<ExternalIssue> executeSensorImporting(@Nullable String fileName) throws IOException {
    return executeSensorImporting(fileName, Collections.emptyMap());
  }

  protected List<ExternalIssue> executeSensorImporting(@Nullable String fileName, Map<String, String> additionalProperties) throws IOException {
    Path projectDir = projectDir();
    var context = createContext(fileName, projectDir, additionalProperties);
    sensor().execute(context);
    return new ArrayList<>(context.allExternalIssues());
  }

  protected SensorContextTester createContext(@Nullable String fileName, Path projectDir, Map<String, String> additionalProperties) throws IOException {
    Path baseDir = projectDir.getParent();
    SensorContextTester context = SensorContextTester.create(baseDir);
    try (Stream<Path> fileStream = Files.list(projectDir)) {
      fileStream.forEach(file -> addFileToContext(context, baseDir, file));
    }
    context.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(8, 9), SonarQubeSide.SERVER, SonarEdition.DEVELOPER));
    if (fileName != null) {
      MapSettings settings = context.settings();
      String path = projectDir.resolve(fileName).toAbsolutePath().toString();
      settings.setProperty("sonar.php." + sensor().reportKey() + ".reportPaths", path);
      additionalProperties.forEach(settings::setProperty);
    }
    return context;
  }

  private static void addFileToContext(SensorContextTester context, Path projectDir, Path file) {
    try {
      String projectId = projectDir.getFileName().toString() + "-project";
      context.fileSystem().add(TestInputFileBuilder.create(projectId, projectDir.toFile(), file.toFile())
        .setCharset(UTF_8)
        .setLanguage(language(file))
        .setContents(new String(Files.readAllBytes(file), UTF_8))
        .setType(InputFile.Type.MAIN)
        .build());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void noIssuesWithoutReportPathsProperty() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(null);
    assertThat(externalIssues).isEmpty();
    assertNoErrorWarnDebugLogs(logTester());
  }

  @Test
  void noIssuesWithInvalidReportPath() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("invalid-path.txt");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester().logs(Level.ERROR)))
      .startsWith("An error occurred when reading report file '")
      .contains("invalid-path.txt', no issue will be imported from this report.\nThe file was not found.");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("An error occurred when reading report file '"));
  }

  @Test
  void noIssuesWithInvalidFile() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("not-" + sensor().reportKey() + "-report.json");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester().logs(Level.ERROR)))
      .startsWith("An error occurred when reading report file '")
      .contains("no issue will be imported from this report.\nThe content of the file probably does not have the expected format.");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("An error occurred when reading report file '"));
  }

  @Test
  void noIssuesWithEmptyFile() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(sensor().reportKey() + "-report-empty.json");
    assertThat(externalIssues).isEmpty();
    assertNoErrorWarnDebugLogs(logTester());
  }

  @Test
  void shouldNotAddImpactsForSonarCloud() throws IOException {
    var projectDir = projectDir();
    var context = createContext(sensor().reportKey() + "-report.json", projectDir, Map.of());
    context.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(8, 0), SonarQubeSide.SCANNER, SonarEdition.SONARCLOUD));

    sensor().execute(context);

    var externalIssues = new ArrayList<>(context.allExternalIssues());
    // Actually, sonar-plugin-api-impl from SonarCloud would return a null here, but we use the dependency from SonarQube,
    // so we only verify that value was not saved.
    assertThat(externalIssues).extracting(ExternalIssue::impacts).isNotEmpty().allMatch(Map::isEmpty);
  }

  @Test
  void shouldAddImpactsForSonarLint() throws IOException {
    var projectDir = projectDir();
    var context = createContext(sensor().reportKey() + "-report.json", projectDir, Map.of());
    context.setRuntime(SonarRuntimeImpl.forSonarLint(Version.create(10, 1)));

    sensor().execute(context);

    var externalIssues = new ArrayList<>(context.allExternalIssues());
    assertThat(externalIssues).extracting(ExternalIssue::impacts).isNotEmpty().allMatch(not(Map::isEmpty));
  }

  protected abstract Path projectDir();

  protected abstract ExternalIssuesSensor sensor();

  protected abstract LogTesterJUnit5 logTester();

}
