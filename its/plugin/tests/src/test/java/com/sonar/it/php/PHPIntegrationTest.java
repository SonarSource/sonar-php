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
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.WsMeasures.Measure;

import static com.sonar.it.php.Tests.getMeasure;
import static com.sonar.it.php.Tests.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class PHPIntegrationTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  public final static String FILE_TOKEN_PARSER = "project" + ":Bridge/Twig/TokenParser/TransChoiceTokenParser.php";

  @BeforeClass
  public static void startServer() throws IOException, URISyntaxException, InterruptedException {
    orchestrator.resetData();

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
    assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(23108d);
    assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(39571d);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(425d);
    assertThat(getProjectMeasureAsDouble("classes")).isEqualTo(405d);
    assertThat(getProjectMeasureAsDouble("functions")).isEqualTo(1882d);

    // Comments
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(17.3);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(4836d);
    assertThat(getProjectMeasureAsDouble("public_documented_api_density")).isNull();
    assertThat(getProjectMeasureAsDouble("public_undocumented_api")).isNull();
    assertThat(getProjectMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasureAsDouble("function_complexity")).isEqualTo(2.3);
    assertThat(getProjectMeasureAsDouble("class_complexity")).isEqualTo(10.1);
    assertThat(getProjectMeasureAsDouble("file_complexity")).isEqualTo(9.7);
    assertThat(getProjectMeasureAsDouble("complexity")).isEqualTo(4110.0);
    assertThat(getProjectMeasure("function_complexity_distribution").getValue()).isEqualTo("1=1236;2=353;4=132;6=71;8=34;10=20;12=36");
    assertThat(getProjectMeasure("file_complexity_distribution").getValue()).isEqualTo("0=198;5=97;10=78;20=29;30=11;60=10;90=2");
    assertThat(getProjectMeasureAsDouble("class_complexity_distribution")).isNull();
  }

  @Test
  public void fileMetrics() {
    // Size
    assertThat(getFileMeasureAsDouble("ncloc")).isEqualTo(41d);
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(90d);
    assertThat(getFileMeasureAsDouble("files")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("classes")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("functions")).isEqualTo(3d);
    assertThat(lineNumbersInDataMeasure(getFileMeasure("ncloc_data").getValue())).isEqualTo(lineNumbersInDataMeasure(
      "12=1;14=1;21=1;22=1;32=1;33=1;34=1;35=1;37=1;39=1;41=1;42=1;44=1;46=1;47=1;48=1;50=1;52=1;53=1;54=1;56=1;58=1;59=1;60=1;62=1;64=1;66=1;67=1;68=1;70=1;72=1;73=1;75=1;76=1;77=1;78=1;85=1;86=1;87=1;88=1;89=1"));
    if (is_after_sonar_6_2()) {
      assertThat(lineNumbersInDataMeasure(getFileMeasure("executable_lines_data").getValue())).isEqualTo(lineNumbersInDataMeasure(
        "12=1;14=1;21=1;22=1;32=1;33=1;34=1;35=1;37=1;39=1;41=1;42=1;44=1;46=1;47=1;48=1;50=1;52=1;53=1;54=1;56=1;58=1;59=1;60=1;62=1;64=1;66=1;67=1;68=1;70=1;72=1;73=1;75=1;76=1;77=1;78=1;85=1;86=1;87=1;88=1;89=1"));
      assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(41d);
      assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(41d);
    }

    // Comments
    assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(26.8);
    assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(15d);
    assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
    assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();

    assertThat(getFileMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasureAsDouble("function_complexity")).isEqualTo(3.0);
    assertThat(getFileMeasureAsDouble("class_complexity")).isEqualTo(9.0);
    assertThat(getFileMeasureAsDouble("file_complexity")).isEqualTo(9.0);
    assertThat(getFileMeasureAsDouble("complexity")).isEqualTo(9.0);
  }

  private Set<Integer> lineNumbersInDataMeasure(String data) {
    Set<Integer> lineNumbers = new HashSet<>();
    for (String lineData : data.split(";")) {
      lineNumbers.add(Integer.valueOf(lineData.replace("=1", "")));
    }
    return lineNumbers;
  }

  /**
   * SONAR-3139
   */
  @Test
  public void testDuplicationResults() throws Exception {
    assertThat(getProjectMeasureAsDouble("duplicated_lines")).isEqualTo(1595d);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isEqualTo(54d);
    assertThat(getProjectMeasureAsDouble("duplicated_files")).isEqualTo(32d);
    assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isEqualTo(4.0);
  }

  /**
   * SONARPHP-278
   */
  @Test
  public void should_be_compatible_with_DevCockpit() {
    assertThat(getFileMeasure("ncloc_data").getValue()).isNotEmpty();
    assertThat(getFileMeasure("comment_lines_data").getValue()).isNotEmpty();
  }

  private Measure getProjectMeasure(String metricKey) {
    return getMeasure("project", metricKey.trim());
  }

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble("project", metricKey.trim());
  }

  private Measure getFileMeasure(String metricKey) {
    return getMeasure(FILE_TOKEN_PARSER, metricKey.trim());
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(FILE_TOKEN_PARSER, metricKey.trim());
  }

  public static boolean is_after_sonar_6_2() {
    return orchestrator.getConfiguration().getSonarVersion().isGreaterThanOrEquals("6.2");
  }

}
