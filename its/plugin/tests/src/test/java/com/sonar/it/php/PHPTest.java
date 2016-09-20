/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import com.sonar.orchestrator.build.SonarScanner;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import static org.fest.assertions.Assertions.assertThat;

public class PHPTest {

  private static final String MULTIMODULE_PROJET_KEY = "multimodule-php";
  private static final String SEVERAL_EXTENSIONS_PROJECT_KEY = "project-with-several-extensions";
  private static final String SRC_DIR_NAME = "src";

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static Sonar sonar;

  @BeforeClass
  public static void startServer() throws IOException, URISyntaxException {
    sonar = orchestrator.getServer().getWsClient();
  }

  /**
   * SONARPLUGINS-1657
   */
  @Test
  public void should_import_sources_with_user_defined_file_suffixes() {
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("project-with-several-extensions"))
      .setProfile("it-profile")
      .setProperty("sonar.php.file.suffixes", "php,php3,php4,myphp,html");
    orchestrator.executeBuild(build);

    assertThat(getResourceMeasure(SEVERAL_EXTENSIONS_PROJECT_KEY, "files").getValue()).isEqualTo(3);
    assertThat(getResourceMeasure(getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math2.myphp"), "lines").getValue()).isGreaterThan(1);
    assertThat(getResource(getResourceKey(SEVERAL_EXTENSIONS_PROJECT_KEY, "Math3.pgp"))).isNull();
  }

  /**
   * SONARPLUGINS-943
   */
  @Test
  public void should_support_multimodule_projects() {
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("multimodule"))
      .setProfile("it-profile");
    orchestrator.executeBuild(build);

    assertThat(getResourceMeasure(MULTIMODULE_PROJET_KEY + ":module1", "files").getValue()).isEqualTo(4);
    assertThat(getResourceMeasure(MULTIMODULE_PROJET_KEY + ":module2", "files").getValue()).isEqualTo(2);
    assertThat(getResourceMeasure(MULTIMODULE_PROJET_KEY, "files").getValue()).isEqualTo(4 + 2);
  }

  private String getResourceKey(String projectKey, String fileName) {
    return projectKey + ":" + SRC_DIR_NAME + "/" + fileName;
  }

  private Resource getResource(String resourceKey) {
    return sonar.find(ResourceQuery.create(resourceKey));
  }

  private Measure getResourceMeasure(String resourceKey, String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(resourceKey, metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

}
