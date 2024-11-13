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
package com.sonar.it.php;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Measures;

import static com.sonar.it.php.Tests.createScanner;
import static com.sonar.it.php.Tests.getMeasure;
import static com.sonar.it.php.Tests.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

class PHPIntegrationTest {

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "php-integration";
  private static final String PROJECT_NAME = "PHP Integration";

  public static final String FILE_TOKEN_PARSER = PROJECT_KEY + ":src/CodeCleaner/CalledClassPass.php";

  @BeforeAll
  static void startServer() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectDir(FileLocation.of("../sources/src/psysh/").getFile())
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.exclusions", "**/Component/**/*.php, **/Bridge/ProxyManager/Tests/LazyProxy/PhpDumper/Fixtures/proxy-implem.php")
      .setProperty("sonar.internal.analysis.failFast", "false");

    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }

  @Test
  void projectMetric() {
    SoftAssertions.assertSoftly(softly -> {
      // Size
      softly.assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(23388d);
      softly.assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(43185d);
      softly.assertThat(getProjectMeasureAsDouble("files")).isEqualTo(304d);
      softly.assertThat(getProjectMeasureAsDouble("classes")).isEqualTo(299d);
      softly.assertThat(getProjectMeasureAsDouble("functions")).isEqualTo(1977d);

      // Comments
      softly.assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(22.8);
      softly.assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(6912d);
      softly.assertThat(getProjectMeasureAsDouble("public_documented_api_density")).isNull();
      softly.assertThat(getProjectMeasureAsDouble("public_undocumented_api")).isNull();
      softly.assertThat(getProjectMeasureAsDouble("public_api")).isNull();

      // Complexity
      softly.assertThat(getProjectMeasureAsDouble("file_complexity")).isEqualTo(14.3);
      softly.assertThat(getProjectMeasureAsDouble("complexity")).isEqualTo(4341d);
      softly.assertThat(getProjectMeasureAsDouble("cognitive_complexity")).isEqualTo(3203d);
    });
  }

  @Test
  void fileMetrics() {
    SoftAssertions.assertSoftly(softly -> {
      // Size
      softly.assertThat(getFileMeasureAsDouble("ncloc")).isEqualTo(48d);
      softly.assertThat(getFileMeasureAsDouble("lines")).isEqualTo(95d);
      softly.assertThat(getFileMeasureAsDouble("files")).isEqualTo(1d);
      softly.assertThat(getFileMeasureAsDouble("classes")).isEqualTo(1d);
      softly.assertThat(getFileMeasureAsDouble("functions")).isEqualTo(4d);
      softly.assertThat(lineNumbersInDataMeasure(getFileMeasure("ncloc_data").getValue()))
        .isEqualTo(Set.of(64, 66, 67, 68, 69, 70, 71, 72, 12, 14, 15, 79, 16, 80, 17, 81, 18, 82, 19, 83, 20, 84, 21, 86, 87, 88, 89, 90,
          27, 28, 92, 29, 93, 94, 36, 37, 38, 39, 48, 49, 50, 51, 52, 57, 58, 59, 62, 63));
      softly.assertThat(lineNumbersInDataMeasure(getFileMeasure("executable_lines_data").getValue()))
        .isEqualTo(Set.of(66, 67, 68, 69, 38, 81, 50, 82, 51, 88, 57, 89, 58, 92, 62, 63));
      softly.assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(16d);
      softly.assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(16d);

      // Comments
      softly.assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(26.2);
      softly.assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(17d);
      softly.assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
      softly.assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();

      softly.assertThat(getFileMeasureAsDouble("public_api")).isNull();

      // Complexity
      softly.assertThat(getFileMeasureAsDouble("file_complexity")).isEqualTo(14d);
      softly.assertThat(getFileMeasureAsDouble("complexity")).isEqualTo(14d);
      softly.assertThat(getFileMeasureAsDouble("cognitive_complexity")).isEqualTo(14d);
    });
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
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(getProjectMeasureAsDouble("duplicated_lines")).isEqualTo(1939);
      softly.assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isEqualTo(53d);
      softly.assertThat(getProjectMeasureAsDouble("duplicated_files")).isEqualTo(29d);
      softly.assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isEqualTo(4.5);
    });
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
