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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.reports.ExternalReportFileHandler;
import org.sonar.plugins.php.reports.phpunit.xml.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

class TestFileReportTest {

  private String componentKey;
  private String testFileName;
  private SensorContextTester context;
  private Set<String> unresolvedInputFiles;

  private ExternalReportFileHandler fileHandler;

  private final Consumer<String> addUnresolvedInputFiles = file -> unresolvedInputFiles.add(file);

  @BeforeEach
  public void setUp() {
    testFileName = "testfile.php";
    DefaultInputFile testFile = TestInputFileBuilder.create("moduleKey", testFileName).setType(InputFile.Type.TEST).setLanguage(Php.KEY).build();
    context = SensorContextTester.create(new File("src/test/resources"));
    context.fileSystem().add(testFile);
    fileHandler = ExternalReportFileHandler.create(context);
    componentKey = testFile.key();
    unresolvedInputFiles = new LinkedHashSet<>();
  }

  @Test
  void shouldReportStatusCounts() {
    final TestFileReport report = new TestFileReport(testFileName, 3d);
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.saveTestMeasures(context, fileHandler, addUnresolvedInputFiles);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_EXECUTION_TIME, 3000l);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.SKIPPED_TESTS, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_ERRORS, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_FAILURES, 2);
    assertThat(unresolvedInputFiles).isEmpty();
  }

  @Test
  void shouldReportZeroTestsIfEmpty() {
    final TestFileReport report = new TestFileReport(testFileName, 0d);
    report.saveTestMeasures(context, fileHandler, addUnresolvedInputFiles);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TESTS, 0);
    assertThat(unresolvedInputFiles).isEmpty();
  }

  @Test
  void shouldNotCountSkippedTests() {
    final TestFileReport report = new TestFileReport(testFileName, 1d);
    report.addTestCase(new TestCase(null, null, null, null, null));
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));
    report.saveTestMeasures(context, fileHandler, addUnresolvedInputFiles);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TESTS, 3);
    assertThat(unresolvedInputFiles).isEmpty();
  }

  @Test
  void shouldGenerateToString() {
    final TestFileReport report = new TestFileReport(testFileName, 2.5);
    report.addTestCase(new TestCase(null, null, null, null, null));
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));

    assertThat(report.toString())
      .startsWith("org.sonar.plugins.php.reports.phpunit.TestFileReport@")
      .endsWith("[errors=1,failures=1,file=testfile.php,skipped=1,tests=4,testDuration=2.5]");
  }

  @Test
  void shouldTestEqualsMethod() {
    final TestFileReport report1 = new TestFileReport(testFileName, 2.5);
    report1.addTestCase(new TestCase(null, null, null, null, null));
    report1.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report1.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report1.addTestCase(new TestCase(TestCase.Status.ERROR));

    final TestFileReport report2 = new TestFileReport(testFileName, 2.5);
    report2.addTestCase(new TestCase(null, null, null, null, null));
    report2.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report2.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report2.addTestCase(new TestCase(TestCase.Status.ERROR));

    assertThat(report1).isEqualTo(report2);
  }
}
