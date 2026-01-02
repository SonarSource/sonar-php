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

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.reports.phpunit.TestFileReport;

import static org.assertj.core.api.Assertions.assertThat;

class TestSuiteTest {

  @Test
  void shouldCreateReportOnlyIfFileBased() {
    final TestSuite fileSuite = new TestSuite("file");
    assertThat(fileSuite.generateReports()).hasSize(1);
    assertThat(fileSuite.generateReports().iterator().next()).isEqualTo(new TestFileReport("file", 0d));
    final TestSuite notFileSuite = new TestSuite(null);
    assertThat(notFileSuite.generateReports()).isEmpty();
  }

  @Test
  void shouldDrillDownUntilItFindsAFileBasedSuite() {
    final TestSuite rootSuite = new TestSuite(null);
    final TestSuite intermediateSuite = new TestSuite(null);
    rootSuite.addNested(intermediateSuite);
    final TestSuite fileSuite = new TestSuite("file");
    intermediateSuite.addNested(fileSuite);
    assertThat(rootSuite.generateReports()).containsOnly(fileSuite.generateReports().iterator().next());
  }

  @Test
  void shouldCreateOneReportForEveryNestedFileBasedSuite() {
    final TestSuite rootSuite = new TestSuite(null);
    final TestSuite fileSuite1 = new TestSuite("file1");
    rootSuite.addNested(fileSuite1);
    final TestSuite intermediateSuite = new TestSuite(null);
    final TestSuite fileSuite2 = new TestSuite("file2");
    intermediateSuite.addNested(fileSuite2);
    rootSuite.addNested(intermediateSuite);
    final Collection<TestFileReport> reports = rootSuite.generateReports();
    assertThat(reports)
      .contains(fileSuite1.generateReports().iterator().next())
      .contains(fileSuite2.generateReports().iterator().next());
  }

  /**
   * This test is checking that we are able to report on something that we have not yet observed as a
   * possible output of phpunit junit result log, a file-based suite within another file-based suite.
   * Feel free to remove this test if it becomes cumbersome in future evolutions.
   *
   */
  @Test
  void shouldCreateAReportForSuiteNestedWithinAnotherFileBasedSuite() {
    final TestSuite fileSuite = new TestSuite("file");
    final TestSuite nestedFileSuite = new TestSuite("nestedFile");
    fileSuite.addNested(nestedFileSuite);
    assertThat(fileSuite.generateReports()).contains(nestedFileSuite.generateReports().iterator().next());
  }
}
