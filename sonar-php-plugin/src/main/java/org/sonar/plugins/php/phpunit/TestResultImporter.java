/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.phpunit.xml.TestSuites;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.xml.ParseException;

public class TestResultImporter extends PhpUnitReportImporter {

  private static final Logger LOG = Loggers.get(TestResultImporter.class);

  private final JUnitLogParserForPhpUnit parser = new JUnitLogParserForPhpUnit();

  public TestResultImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  protected void importReport(File reportFile, SensorContext context) throws ParseException, IOException {
    LOG.info("Importing {}", reportFile);
    TestSuites testSuites = parser.parse(reportFile);
    for (TestFileReport fileReport : testSuites.arrangeSuitesIntoTestFileReports()) {
      fileReport.saveTestMeasures(context, unresolvedInputFiles);
    }
  }

  /**
   * For PHPUnit tests report only a single file is expected compared to PHPUnit coverage reports
   */
  @Override
  protected List<File> reportFiles(SensorContext context) {
    return context.config().get(reportPathKey())
      .map(report -> getIOFile(context.fileSystem().baseDir(), report))
      .map(Collections::singletonList)
      .orElse(Collections.emptyList());
  }

  /**
   * Inspired by {@link org.sonarsource.analyzer.commons.ExternalReportProvider}
   */
  private static File getIOFile(File baseDir, String path) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(baseDir, path);
    }
    return file;
  }

  @Override
  String reportPathKey() {
    return PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY;
  }

  @Override
  String reportName() {
    return "PHPUnit tests";
  }

  @Override
  Logger logger() {
    return LOG;
  }
}
