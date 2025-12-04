/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.xml.ParseException;

public class TestResultImporter extends PhpUnitReportImporter {

  private static final Logger LOG = LoggerFactory.getLogger(TestResultImporter.class);
  private static final String TEST_REPORT_DOES_NOT_CONTAIN_ANY_RECORD = "PHPUnit test report does not contain any record in file %s.";

  private final JUnitLogParserForPhpUnit parser = new JUnitLogParserForPhpUnit();

  public TestResultImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  public void importReport(File report, SensorContext context) throws ParseException, IOException {
    LOG.info("Importing {}", report);
    var testSuites = parser.parse(report);
    List<TestFileReport> testFileReports = testSuites.arrangeSuitesIntoTestFileReports();
    if (testFileReports.isEmpty()) {
      createWarning(TEST_REPORT_DOES_NOT_CONTAIN_ANY_RECORD, report);
    } else {
      saveTestReports(context, testFileReports);
    }
  }

  private void saveTestReports(SensorContext context, List<TestFileReport> testFileReports) {
    for (TestFileReport fileReport : testFileReports) {
      fileReport.saveTestMeasures(context, fileHandler, this::addUnresolvedInputFile);
    }
  }

  @Override
  public String reportPathKey() {
    return PhpUnitSensor.PHPUNIT_TESTS_REPORT_PATH_KEY;
  }

  @Override
  public String reportName() {
    return "PHPUnit tests";
  }

  @Override
  public Logger logger() {
    return LOG;
  }
}
