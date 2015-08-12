/*
 * PHP :: Integration Tests
 * Copyright (C) 2011 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
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

  public final static String PROJECT_KEY = "com.sonarsource.it.php:symfony-lite";
  public final static String FILE_KEY = PROJECT_KEY + ":Components/Console/Application.php";
  private static Sonar sonar;

  @BeforeClass
  public static void startServer() throws IOException, URISyntaxException {
    orchestrator.resetData();
    sonar = orchestrator.getServer().getWsClient();

    SonarRunner build = SonarRunner.create()
      .setProjectDir(new File("projects/symfony-lite/"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0")
      .setSourceDirs("Components")
      .setProfile("it-profile");

    orchestrator.executeBuild(build);
  }

  @Test
  public void projectMetric() {
    // Size
    assertThat(getProjectMeasure("ncloc").getIntValue()).isEqualTo(2346);
    assertThat(getProjectMeasure("lines").getIntValue()).isEqualTo(5322);
    assertThat(getProjectMeasure("files").getIntValue()).isEqualTo(30);
    assertThat(getProjectMeasure("classes").getIntValue()).isEqualTo(31);
    assertThat(getProjectMeasure("functions").getIntValue()).isEqualTo(256);

    // Comments
    assertThat(getProjectMeasure("comment_lines_density").getValue()).isEqualTo(31.5);
    assertThat(getProjectMeasure("comment_lines").getIntValue()).isEqualTo(1078);
    assertThat(getProjectMeasure("public_documented_api_density")).isNull();
    assertThat(getProjectMeasure("public_undocumented_api")).isNull();
    assertThat(getProjectMeasure("commented_out_code_lines").getIntValue()).isEqualTo(0);
    assertThat(getProjectMeasure("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasure("function_complexity").getValue()).isEqualTo(2.7);
    assertThat(getProjectMeasure("class_complexity").getValue()).isEqualTo(22.2);
    assertThat(getProjectMeasure("file_complexity").getValue()).isEqualTo(23);
    assertThat(getProjectMeasure("complexity").getValue()).isEqualTo(689);
    assertThat(getProjectMeasure("function_complexity_distribution").getData()).isEqualTo("1=146;2=47;4=35;6=11;8=6;10=4;12=7");
    assertThat(getProjectMeasure("file_complexity_distribution").getData()).isEqualTo("0=5;5=8;10=6;20=6;30=3;60=0;90=2");
    assertThat(getProjectMeasure("class_complexity_distribution")).isNull();
  }

  @Test
  public void fileMetrics() {
    // Size
    assertThat(getFileMeasure("ncloc").getIntValue()).isEqualTo(507);
    assertThat(getFileMeasure("lines").getIntValue()).isEqualTo(926);
    assertThat(getFileMeasure("files").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("classes").getIntValue()).isEqualTo(2);
    assertThat(getFileMeasure("functions").getIntValue()).isEqualTo(38);

    // Comments
    assertThat(getFileMeasure("comment_lines_density").getValue()).isEqualTo(21.3);
    assertThat(getFileMeasure("comment_lines").getIntValue()).isEqualTo(137);
    assertThat(getFileMeasure("public_documented_api_density")).isNull();
    assertThat(getFileMeasure("public_undocumented_api")).isNull();
    assertThat(getFileMeasure("commented_out_code_lines")).isNull();
    assertThat(getFileMeasure("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasure("function_complexity").getValue()).isEqualTo(4.3);
    assertThat(getFileMeasure("class_complexity").getValue()).isEqualTo(76.5);
    assertThat(getFileMeasure("file_complexity").getValue()).isEqualTo(153);
    assertThat(getFileMeasure("complexity").getValue()).isEqualTo(153);
  }

  /**
   * SONAR-3139
   */
  @Test
  public void testFileDuplicationResults() throws Exception {
    assertThat(getFileMeasure("duplicated_lines").getIntValue()).isEqualTo(27);
    assertThat(getFileMeasure("duplicated_blocks").getIntValue()).isEqualTo(2);
    assertThat(getFileMeasure("duplicated_files").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("duplicated_lines_density").getValue()).isEqualTo(2.9);
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
    Resource resource = sonar.find(ResourceQuery.createForMetrics(PROJECT_KEY, metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

  private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_KEY, metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

}
