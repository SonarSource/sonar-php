/*
 * Copyright (C) 2011-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
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

import static org.fest.assertions.Assertions.assertThat;

public class PHPIntegrationTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  public final static String PROJECT_SYMFONY = "com.sonarsource.it.php:symfony2-lite";
  public final static String FILE_TOKEN_PARSER = PROJECT_SYMFONY + ":src/Symfony/Bridge/Twig/TokenParser/TransChoiceTokenParser.php";
  private static Sonar sonar;

  @BeforeClass
  public static void startServer() throws IOException, URISyntaxException {
    orchestrator.resetData();
    sonar = orchestrator.getServer().getWsClient();

    SonarRunner build = SonarRunner.create()
      .setProjectDir(new File("projects/symfony-lite/"))
      .setProfile("it-profile")
      .setProperty("sonar.exclusions", "**/Symfony/Component/**/*.php");

    orchestrator.executeBuild(build);
  }

  @Test
  public void projectMetric() {
    // Size
    assertThat(getProjectMeasure("ncloc").getIntValue()).isEqualTo(16148);
    assertThat(getProjectMeasure("lines").getIntValue()).isEqualTo(27761);
    assertThat(getProjectMeasure("files").getIntValue()).isEqualTo(353);
    assertThat(getProjectMeasure("classes").getIntValue()).isEqualTo(306);
    assertThat(getProjectMeasure("functions").getIntValue()).isEqualTo(1205);

    // Comments
    assertThat(getProjectMeasure("comment_lines_density").getValue()).isEqualTo(17.6);
    assertThat(getProjectMeasure("comment_lines").getIntValue()).isEqualTo(3454);
    assertThat(getProjectMeasure("public_documented_api_density")).isNull();
    assertThat(getProjectMeasure("public_undocumented_api")).isNull();
    assertThat(getProjectMeasure("commented_out_code_lines").getIntValue()).isEqualTo(13);
    assertThat(getProjectMeasure("public_api")).isNull();

    // Complexity
    if (Tests.is_after_plugin("2.4.1")) {
      assertThat(getProjectMeasure("function_complexity").getValue()).isEqualTo(2.5);
    } else {
      assertThat(getProjectMeasure("function_complexity").getValue()).isEqualTo(2.3);
    }
    assertThat(getProjectMeasure("class_complexity").getValue()).isEqualTo(9.2);
    assertThat(getProjectMeasure("file_complexity").getValue()).isEqualTo(8.1);
    assertThat(getProjectMeasure("complexity").getValue()).isEqualTo(2874.0);
    assertThat(getProjectMeasure("function_complexity_distribution").getData()).isEqualTo("1=736;2=234;4=113;6=48;8=30;10=21;12=23");
    assertThat(getProjectMeasure("file_complexity_distribution").getData()).isEqualTo("0=190;5=68;10=56;20=20;30=16;60=2;90=1");
    assertThat(getProjectMeasure("class_complexity_distribution")).isNull();
  }

  @Test
  public void fileMetrics() {
    // Size
    assertThat(getFileMeasure("ncloc").getIntValue()).isEqualTo(41);
    assertThat(getFileMeasure("lines").getIntValue()).isEqualTo(88);
    assertThat(getFileMeasure("files").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("classes").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("functions").getIntValue()).isEqualTo(3);

    // Comments
    assertThat(getFileMeasure("comment_lines_density").getValue()).isEqualTo(25.5);
    assertThat(getFileMeasure("comment_lines").getIntValue()).isEqualTo(14);
    assertThat(getFileMeasure("public_documented_api_density")).isNull();
    assertThat(getFileMeasure("public_undocumented_api")).isNull();
    assertThat(getFileMeasure("commented_out_code_lines").getIntValue()).isEqualTo(3);
    assertThat(getFileMeasure("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasure("function_complexity").getValue()).isEqualTo(3.0);
    assertThat(getFileMeasure("class_complexity").getValue()).isEqualTo(9.0);
    assertThat(getFileMeasure("file_complexity").getValue()).isEqualTo(9.0);
    assertThat(getFileMeasure("complexity").getValue()).isEqualTo(9.0);
  }

  /**
   * SONAR-3139
   */
  @Test
  public void testFileDuplicationResults() throws Exception {
    assertThat(getFileMeasure("duplicated_lines").getIntValue()).isEqualTo(16);
    assertThat(getFileMeasure("duplicated_blocks").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("duplicated_files").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("duplicated_lines_density").getValue()).isEqualTo(18.2);
  }

  /**
   * SONARPHP-278
   */
  @Test
  public void should_be_compatible_with_DevCockpit() {
    assertThat(getFileMeasure("ncloc_data").getData()).isNotEmpty();
    assertThat(getFileMeasure("comment_lines_data").getData()).isNotEmpty();
  }

  private Measure getProjectMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(PROJECT_SYMFONY, metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

  private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_TOKEN_PARSER, metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

}
