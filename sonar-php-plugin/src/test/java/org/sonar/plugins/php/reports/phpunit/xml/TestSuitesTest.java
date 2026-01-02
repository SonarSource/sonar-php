/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports.phpunit.xml;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.reports.phpunit.TestFileReport;

import static org.assertj.core.api.Assertions.assertThat;

class TestSuitesTest {

  @Test
  void shouldCollectReportsFromAllTestSuites() {
    final String testFile1 = "one.php";
    final String testFile2 = "two.php";
    final TestSuites testSuites = new TestSuites(Arrays.asList(new TestSuite(testFile1), new TestSuite(testFile2)));
    final List<TestFileReport> reports = testSuites.arrangeSuitesIntoTestFileReports();
    assertThat(reports).containsExactly(
      new TestFileReport(testFile1, 0d),
      new TestFileReport(testFile2, 0d));
  }

}
