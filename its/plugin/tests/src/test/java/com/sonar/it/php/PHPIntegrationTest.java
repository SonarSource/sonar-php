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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Measures;

import static com.sonar.it.php.Tests.getMeasure;
import static com.sonar.it.php.Tests.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class PHPIntegrationTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "php-integration";
  private static final String PROJECT_NAME = "PHP Integration";

  public final static String FILE_TOKEN_PARSER = PROJECT_KEY + ":Bridge/Twig/TokenParser/TransChoiceTokenParser.php";

  @BeforeClass
  public static void startServer() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProjectDir(FileLocation.of("../../sources/src/Symfony/").getFile())
      .setProperty("sonar.exclusions", "**/Component/**/*.php");

    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }

  @Test
  public void projectMetric() {
    // Size
    assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(47539d);
    assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(72332d);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(783d);
    assertThat(getProjectMeasureAsDouble("classes")).isEqualTo(645d);
    assertThat(getProjectMeasureAsDouble("functions")).isEqualTo(3394d);

    // Comments
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(11.6);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(6232d);
    assertThat(getProjectMeasureAsDouble("public_documented_api_density")).isNull();
    assertThat(getProjectMeasureAsDouble("public_undocumented_api")).isNull();
    assertThat(getProjectMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getProjectMeasureAsDouble("file_complexity")).isEqualTo(8.6);
    assertThat(getProjectMeasureAsDouble("complexity")).isEqualTo(6702.0);
    assertThat(getProjectMeasureAsDouble("cognitive_complexity")).isEqualTo(5080.0);
  }

  @Test
  public void fileMetrics() {
    // Size
    assertThat(getFileMeasureAsDouble("ncloc")).isEqualTo(48d);
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(98d);
    assertThat(getFileMeasureAsDouble("files")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("classes")).isEqualTo(1d);
    assertThat(getFileMeasureAsDouble("functions")).isEqualTo(3d);
    assertThat(lineNumbersInDataMeasure(getFileMeasure("ncloc_data").getValue()))
      .isEqualTo(lineNumbersInDataMeasure("64=1;66=1;67=1;68=1;70=1;72=1;74=1;75=1;76=1;12=1;78=1;14=1;15=1;80=1;16=1;81=1;17=1;18=1;83=1;19=1;84=1;20=1;85=1;86=1;93=1;29=1;94=1;30=1;95=1;96=1;97=1;38=1;39=1;40=1;41=1;43=1;45=1;47=1;49=1;50=1;52=1;54=1;55=1;56=1;58=1;60=1;61=1;62=1"));
    assertThat(lineNumbersInDataMeasure(getFileMeasure("executable_lines_data").getValue()))
      .isEqualTo(lineNumbersInDataMeasure("64=1;66=1;67=1;70=1;72=1;40=1;41=1;74=1;75=1;43=1;45=1;78=1;47=1;80=1;49=1;50=1;52=1;85=1;54=1;55=1;58=1;60=1;61=1;95=1"));
    assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(24d);
    assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(24d);

    // Comments
    assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(23.8);
    assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(15d);
    assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
    assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();

    assertThat(getFileMeasureAsDouble("public_api")).isNull();

    // Complexity
    assertThat(getFileMeasureAsDouble("file_complexity")).isEqualTo(8.0);
    assertThat(getFileMeasureAsDouble("complexity")).isEqualTo(8.0);
    assertThat(getFileMeasureAsDouble("cognitive_complexity")).isEqualTo(5.0);
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
  public void testDuplicationResults() {
    assertThat(getProjectMeasureAsDouble("duplicated_lines")).isEqualTo(3766d);
    assertThat(getProjectMeasureAsDouble("duplicated_blocks")).isEqualTo(150d);
    assertThat(getProjectMeasureAsDouble("duplicated_files")).isEqualTo(55d);
    assertThat(getProjectMeasureAsDouble("duplicated_lines_density")).isEqualTo(5.2);
  }

  /**
   * SONARPHP-278
   */
  @Test
  public void should_be_compatible_with_DevCockpit() {
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
