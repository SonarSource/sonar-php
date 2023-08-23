/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2023 SonarSource SA
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
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.container.Server;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.junit5.OrchestratorExtensionBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Components;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.ce.CeService;
import org.sonarqube.ws.client.ce.TaskRequest;
import org.sonarqube.ws.client.components.TreeRequest;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentRequest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class Tests {

  public static final String PROJECT_ROOT_DIR = "../projects/";

  private static final String RESOURCE_DIRECTORY = "/com/sonar/it/php/";

  public static final String PHP_SENSOR_NAME = "PHP sensor";

  public static final String PHP_INI_SENSOR_NAME = "Analyzer for \"php.ini\" files";

  @RegisterExtension
  public static final Orchestrator ORCHESTRATOR;

  public static final FileLocation PHP_PLUGIN_LOCATION = FileLocation.byWildcardMavenFilename(new File("../../../sonar-php-plugin/target"), "sonar-php-plugin-*.jar");

  private static final TaskRequest TASK_REQUEST = new TaskRequest().setAdditionalFields(Collections.singletonList("warnings"));

  private static final Pattern TASK_ID_PATTERN = Pattern.compile("/api/ce/task\\?id=(\\S+)");

  static {
    OrchestratorBuilder<OrchestratorExtensionBuilder, OrchestratorExtension> builder = OrchestratorExtension.builderEnv()
      .useDefaultAdminCredentialsForBuilds(true)
      .setSonarVersion(System.getProperty("sonar.runtimeVersion", "LATEST_RELEASE"))
      // PHP Plugin
      .addPlugin(PHP_PLUGIN_LOCATION)
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "profile.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "no_rules.xml"))
      // Custom rules plugin
      .addPlugin(FileLocation.byWildcardMavenFilename(new File("../plugins/php-custom-rules-plugin/target"), "php-custom-rules-plugin-*.jar"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "profile-php-custom-rules.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "nosonar.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "sleep.xml"));
    ORCHESTRATOR = builder.build();
  }

  public static void provisionProject(String projectKey, String projectName, String languageKey, String profileName) {
    Server server = ORCHESTRATOR.getServer();
    server.provisionProject(projectKey, projectName);
    server.associateProjectToQualityProfile(projectKey, languageKey, profileName);
  }

  static File projectDirectoryFor(String projectDirName) {
    return new File(Tests.PROJECT_ROOT_DIR + projectDirName + "/");
  }

  @CheckForNull
  static Measures.Measure getMeasure(String componentKey, String metricKey) {
    Measures.ComponentWsResponse response = newWsClient().measures().component(new ComponentRequest()
      .setComponent(componentKey)
      .setMetricKeys(singletonList(metricKey)));
    List<Measures.Measure> measures = response.getComponent().getMeasuresList();
    return measures.size() == 1 ? measures.get(0) : null;
  }

  @CheckForNull
  static Integer getMeasureAsInt(String componentKey, String metricKey) {
    Measures.Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Integer.parseInt(measure.getValue());
  }

  @CheckForNull
  static Double getMeasureAsDouble(String componentKey, String metricKey) {
    Measures.Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Double.parseDouble(measure.getValue());
  }

  @CheckForNull
  static Components.Component getComponent(String projectKey, String componentKey) {
    List<Components.Component> components = newWsClient().components().tree(new TreeRequest()
      .setComponent(projectKey)
      .setQ(componentKey))
      .getComponentsList();
    return components.size() == 1 ? components.get(0) : null;
  }

  /**
   * Extract analysis warnings from component task to evaluate if expected warnings are send to the server
   */
  static List<String> getAnalysisWarnings(BuildResult result) {
    String taskId = getTaskId(result);
    if (taskId == null) {
      throw new RuntimeException("Task id can not be processed from BuildResult");
    }
    CeService service = newWsClient().ce();
    return service.task(TASK_REQUEST.setId(taskId)).getTask().getWarningsList();
  }

  @CheckForNull
  static String getTaskId(BuildResult result) {
    Matcher m = TASK_ID_PATTERN.matcher(result.getLogs());
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  static WsClient newWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .credentials("admin", "admin")
      .build());
  }

  static List<Issues.Issue> issuesForComponent(String componentKey) {
    return newWsClient()
      .issues()
      .search(new SearchRequest().setComponentKeys(Collections.singletonList(componentKey)))
      .getIssuesList();
  }

  static List<Issues.Issue> issuesForRule(List<Issues.Issue> issues, String ruleKey) {
    return issues.stream()
      .filter(i -> i.getRule().equals(ruleKey))
      .collect(Collectors.toList());
  }

  public static void executeBuildWithExpectedWarnings(Orchestrator orchestrator, SonarScanner build) {
    BuildResult result = orchestrator.executeBuild(build);
    assertAnalyzerLogs(result.getLogs());
  }

  private static void assertAnalyzerLogs(String logs) {
    assertThat(logs).contains(PHP_SENSOR_NAME);
    assertThat(logs).contains(PHP_INI_SENSOR_NAME);

    List<String> lines = Arrays.asList(logs.split("[\r\n]+"));

    assertThat(lines.size()).isBetween(25, 150);

    List<String> unexpectedLogs = lines.stream()
      .filter(line -> !line.startsWith("INFO: "))
      .filter(line -> !line.startsWith("WARN: sonar.php.coverage.reportPath is deprecated as of SonarQube 6.2"))
      .filter(line -> !line.startsWith("WARN: sonar.php.coverage.itReportPath is deprecated as of SonarQube 6.2"))
      .filter(line -> !line.startsWith("WARN: sonar.php.coverage.overallReportPath is deprecated as of SonarQube 6.2"))
      .filter(line -> !line.startsWith("WARN: Line with number 0 doesn't belong to file Math.php"))
      .filter(line -> !line.startsWith("WARN: Line with number 100 doesn't belong to file Math.php"))
      .filter(line -> !line.startsWith("WARN: SonarQube scanners will require Java 11+ starting on next version"))
      .filter(line -> !line.startsWith("WARN: The sonar.modules is a deprecated property and should not be used anymore"))
      .filter(line -> !line.startsWith("WARN: PHPUnit test cases are detected. Make sure to specify test sources via `sonar.test` to get more precise analysis results."))
      .filter(line -> !line.startsWith("WARNING: An illegal reflective access operation has occurred"))
      .filter(line -> !line.startsWith("WARNING: Illegal reflective access"))
      .filter(line -> !line.startsWith("WARNING: Please consider reporting this to the maintainers"))
      .filter(line -> !line.startsWith("WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations"))
      .filter(line -> !line.startsWith("WARNING: All illegal access operations will be denied in a future release"))
      .filter(line -> !line.startsWith("Picked up JAVA_TOOL_OPTIONS:"))
      .collect(Collectors.toList());

    assertThat(unexpectedLogs).isEmpty();
  }
}
