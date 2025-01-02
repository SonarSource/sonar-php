/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package com.sonar.it.php;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.sonar.it.php.Tests.createScanner;
import static com.sonar.it.php.Tests.getMeasureAsDouble;

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

      softly.assertThat(getFileMeasureAsDouble("lines_to_cover")).isEqualTo(16d);
      softly.assertThat(getFileMeasureAsDouble("uncovered_lines")).isEqualTo(16d);

      // Comments
      softly.assertThat(getFileMeasureAsDouble("comment_lines_density")).isEqualTo(26.2);
      softly.assertThat(getFileMeasureAsDouble("comment_lines")).isEqualTo(17d);
      softly.assertThat(getFileMeasureAsDouble("public_documented_api_density")).isEqualTo(100);
      softly.assertThat(getFileMeasureAsDouble("public_undocumented_api")).isZero();

      softly.assertThat(getFileMeasureAsDouble("public_api")).isNull();

      // Complexity
      softly.assertThat(getFileMeasureAsDouble("complexity")).isEqualTo(14d);
      softly.assertThat(getFileMeasureAsDouble("cognitive_complexity")).isEqualTo(14d);
    });
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

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(PROJECT_KEY, metricKey.trim());
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(FILE_TOKEN_PARSER, metricKey.trim());
  }
}
