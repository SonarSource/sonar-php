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
import com.sonar.orchestrator.locator.FileLocation;
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

public class PHPIntegrationTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  public final static String FILE_TOKEN_PARSER = "project" + ":Bridge/Twig/TokenParser/TransChoiceTokenParser.php";
  private static Sonar sonar;

  @BeforeClass
  public static void startServer() throws IOException, URISyntaxException, InterruptedException {
    orchestrator.resetData();
    sonar = orchestrator.getServer().getWsClient();

    SonarScanner build = SonarScanner.create()
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(FileLocation.of("../../sources/src/symfony/Symfony/").getFile())
      .setProfile("it-profile")
      .setProperty("sonar.exclusions", "**/Component/**/*.php");

    orchestrator.executeBuild(build);
  }

  @Test
  public void projectMetric() {
    // Size
    assertThat(getProjectMeasure("ncloc").getIntValue()).isEqualTo(23108);
    assertThat(getProjectMeasure("lines").getIntValue()).isEqualTo(39571);
    assertThat(getProjectMeasure("files").getIntValue()).isEqualTo(425);
    assertThat(getProjectMeasure("classes").getIntValue()).isEqualTo(405);
    assertThat(getProjectMeasure("functions").getIntValue()).isEqualTo(1882);

    // Comments
    assertThat(getProjectMeasure("comment_lines_density").getValue()).isEqualTo(17.3);
    assertThat(getProjectMeasure("comment_lines").getIntValue()).isEqualTo(4836);
    assertThat(getProjectMeasure("public_documented_api_density")).isNull();
    assertThat(getProjectMeasure("public_undocumented_api")).isNull();
    assertThat(getProjectMeasure("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasure("function_complexity").getValue()).isEqualTo(2.3);
    assertThat(getProjectMeasure("class_complexity").getValue()).isEqualTo(10.1);
    assertThat(getProjectMeasure("file_complexity").getValue()).isEqualTo(9.7);
    assertThat(getProjectMeasure("complexity").getValue()).isEqualTo(4110.0);
    assertThat(getProjectMeasure("function_complexity_distribution").getData()).isEqualTo("1=1236;2=353;4=132;6=71;8=34;10=20;12=36");
    assertThat(getProjectMeasure("file_complexity_distribution").getData()).isEqualTo("0=198;5=97;10=78;20=29;30=11;60=10;90=2");
    assertThat(getProjectMeasure("class_complexity_distribution")).isNull();
  }

  @Test
  public void fileMetrics() {
    // Size
    assertThat(getFileMeasure("ncloc").getIntValue()).isEqualTo(41);
    assertThat(getFileMeasure("lines").getIntValue()).isEqualTo(90);
    assertThat(getFileMeasure("files").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("classes").getIntValue()).isEqualTo(1);
    assertThat(getFileMeasure("functions").getIntValue()).isEqualTo(3);

    // Comments
    assertThat(getFileMeasure("comment_lines_density").getValue()).isEqualTo(26.8);
    assertThat(getFileMeasure("comment_lines").getIntValue()).isEqualTo(15);
    assertThat(getFileMeasure("public_documented_api_density")).isNull();
    assertThat(getFileMeasure("public_undocumented_api")).isNull();

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
    assertThat(getFileMeasure("duplicated_lines_density").getValue()).isEqualTo(17.8);
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
    Resource resource = sonar.find(ResourceQuery.createForMetrics("project", metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

  private Measure getFileMeasure(String metricKey) {
    Resource resource = sonar.find(ResourceQuery.createForMetrics(FILE_TOKEN_PARSER, metricKey.trim()));
    return resource == null ? null : resource.getMeasure(metricKey.trim());
  }

}
