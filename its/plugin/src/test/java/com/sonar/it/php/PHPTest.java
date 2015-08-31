/*
 * Copyright (C) 2011-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
    SonarRunner build = SonarRunner.create()
      .setProjectDir(new File("projects/project-with-several-extensions/"))
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
    SonarRunner build = SonarRunner.create()
      .setProjectDir(new File("projects/multimodule/"))
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
