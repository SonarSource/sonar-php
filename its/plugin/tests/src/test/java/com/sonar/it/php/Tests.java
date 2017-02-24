/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2017 SonarSource SA
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
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.util.List;
import javax.annotation.CheckForNull;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.sonarqube.ws.WsComponents.Component;
import org.sonarqube.ws.WsMeasures;
import org.sonarqube.ws.WsMeasures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.component.TreeWsRequest;
import org.sonarqube.ws.client.measure.ComponentWsRequest;

import static java.util.Collections.singletonList;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  PHPTest.class,
  PHPIntegrationTest.class,
  PHPUnitTest.class,
  CommonRulesTest.class,
  CustomRulesTest.class,
  ReportWithUnresolvedPathTest.class
})
public class Tests {

  public static final String PROJECT_ROOT_DIR = "../projects/";

  private static final String RESOURCE_DIRECTORY = "/com/sonar/it/php/";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR;

  public static final FileLocation PHP_PLUGIN_LOCATION = FileLocation.byWildcardMavenFilename(new File("../../../sonar-php-plugin/target"), "sonar-php-plugin-*.jar");

  static {
    OrchestratorBuilder orchestratorBuilder = Orchestrator.builderEnv()
      // PHP Plugin
      .addPlugin(PHP_PLUGIN_LOCATION)
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "profile.xml"))
      // Custom rules plugin
      .addPlugin(FileLocation.byWildcardMavenFilename(new File("../plugins/php-custom-rules-plugin/target"), "php-custom-rules-plugin-*.jar"))
      .restoreProfileAtStartup(FileLocation.ofClasspath(RESOURCE_DIRECTORY + "profile-php-custom-rules.xml"));
    ORCHESTRATOR = orchestratorBuilder.build();
  }

  public static final File projectDirectoryFor(String projectDirName) {
    return new File(Tests.PROJECT_ROOT_DIR + projectDirName + "/");
  }

  @CheckForNull
  static Measure getMeasure(String componentKey, String metricKey) {
    WsMeasures.ComponentWsResponse response = newWsClient().measures().component(new ComponentWsRequest()
      .setComponentKey(componentKey)
      .setMetricKeys(singletonList(metricKey)));
    List<Measure> measures = response.getComponent().getMeasuresList();
    return measures.size() == 1 ? measures.get(0) : null;
  }

  @CheckForNull
  static Integer getMeasureAsInt(String componentKey, String metricKey) {
    Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Integer.parseInt(measure.getValue());
  }

  @CheckForNull
  static Double getMeasureAsDouble(String componentKey, String metricKey) {
    Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Double.parseDouble(measure.getValue());
  }

  @CheckForNull
  static Component getComponent(String projectKey, String componentKey) {
    List<Component> components = newWsClient().components().tree(
      new TreeWsRequest()
        .setBaseComponentKey(projectKey)
        .setQuery(componentKey))
      .getComponentsList();
    return components.size() == 1 ? components.get(0) : null;
  }

  static WsClient newWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .build());
  }
}
