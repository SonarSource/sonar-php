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
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Measures;

import static com.sonar.it.php.Tests.getMeasure;
import static com.sonar.it.php.Tests.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

class PHPIntegrationTest {

  @RegisterExtension
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "php-integration";
  private static final String PROJECT_NAME = "PHP Integration";

  public static final String FILE_TOKEN_PARSER = PROJECT_KEY + ":Bridge/Twig/TokenParser/TransTokenParser.php";

  @BeforeAll
  static void startServer() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(FileLocation.of("../../sources/src/Symfony/").getFile())
      .setProperty("sonar.exclusions", "**/Component/**/*.php, **/Bridge/ProxyManager/Tests/LazyProxy/PhpDumper/Fixtures/proxy-implem.php")
      .setProperty("sonar.internal.analysis.failFast", "false");

    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }

  @Test
  void projectMetric() {
    // Size
    assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(83800d);
    assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(119844d);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(1146d);
    assertThat(getProjectMeasureAsDouble("classes")).isEqualTo(974d);
    assertThat(getProjectMeasureAsDouble("functions")).isEqualTo(5591d);

    // Comments
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(8.7);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(7996d);
    assertThat(getProjectMeasureAsDouble("public_documented_api_density")).isNull();
    assertThat(getProjectMeasureAsDouble("public_undocumented_api")).isNull();
    assertThat(getProjectMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasureAsDouble("file_complexity")).isEqualTo(9d);
    assertThat(getProjectMeasureAsDouble("complexity")).isEqualTo(10322d);
    assertThat(getProjectMeasureAsDouble("cognitive_complexity")).isEqualTo(7404d);
  }

  @Test
  void fileMetrics() {
    // Size
    assertThat(getFileMeasureAsDouble("ncloc")).isEqualTo(56d);
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(90d);
    assertThat(getFileMeasureAsDouble("files")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("classes")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("functions")).isEqualTo(3d);
    assertThat(lineNumbersInDataMeasure(getFileMeasure("ncloc_data").getValue()))
      .isEqualTo(lineNumbersInDataMeasure(
        "12=1;14=1;15=1;16=1;17=1;18=1;19=1;20=1;21=1;28=1;29=1;30=1;31=1;32=1;33=1;35=1;36=1;37=1;38=1;39=1;40=1;42=1;43=1;44=1;46=1;48=1;49=1;50=1;52=1;54=1;55=1;56=1;58=1;60=1;61=1;62=1;63=1;64=1;65=1;68=1;69=1;71=1;72=1;73=1;75=1;77=1;78=1;80=1;81=1;82=1;83=1;85=1;86=1;87=1;88=1;89=1"));
    assertThat(lineNumbersInDataMeasure(getFileMeasure("executable_lines_data").getValue()))
      .isEqualTo(
        lineNumbersInDataMeasure("68=1;69=1;71=1;72=1;75=1;77=1;82=1;87=1;32=1;33=1;35=1;36=1;37=1;38=1;39=1;40=1;42=1;43=1;46=1;48=1;49=1;52=1;54=1;55=1;58=1;60=1;61=1;63=1"));
    assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(28d);
    assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(28d);

    // Comments
    assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(16.4);
    assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(11d);
    assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
    assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();

    assertThat(getFileMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasureAsDouble("file_complexity")).isEqualTo(10d);
    assertThat(getFileMeasureAsDouble("complexity")).isEqualTo(10d);
    assertThat(getFileMeasureAsDouble("cognitive_complexity")).isEqualTo(12d);
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
  void testDuplicationResults() {
    assertThat(getProjectMeasureAsDouble("duplicated_lines")).isEqualTo(8469d);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isEqualTo(295d);
    assertThat(getProjectMeasureAsDouble("duplicated_files")).isEqualTo(82d);
    assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isEqualTo(7.1);
  }

  /**
   * SONARPHP-278
   */
  @Test
  void shouldBeCompatibleWithDevCockpit() {
    assertThat(getFileMeasure("ncloc_data").getValue()).isNotEmpty();
  }

  private Measures.Measure getProjectMeasure(String metricKey) {
    return getMeasure(PROJECT_KEY, metricKey.trim());
  }

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(PROJECT_KEY, metricKey.trim());
  }

  private Measures.Measure getFileMeasure(String metricKey) {
    return getMeasure(FILE_TOKEN_PARSER, metricKey.trim());
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(FILE_TOKEN_PARSER, metricKey.trim());
  }
}
