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
package org.sonar.plugins.php.reports.phpunit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

public class PhpUnitSensor implements Sensor {

  public static final String PHPUNIT_COVERAGE_REPORT_PATHS_KEY = "sonar.php.coverage.reportPaths";
  public static final String PHPUNIT_TESTS_REPORT_PATH_KEY = "sonar.php.tests.reportPath";
  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitSensor.class);
  private static final FilePredicate PHPUNIT_TEST_FILE_PREDICATE = new TestCaseFilePredicate();
  private final TestResultImporter testResultImporter;
  private final CoverageResultImporter coverageResultImporter;

  public PhpUnitSensor(AnalysisWarningsWrapper analysisWarningsWrapper) {
    this.testResultImporter = new TestResultImporter(analysisWarningsWrapper);
    this.coverageResultImporter = new CoverageResultImporter(analysisWarningsWrapper);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(Php.KEY)
      .name("PHPUnit report sensor");
  }

  @Override
  public void execute(SensorContext context) {
    testResultImporter.execute(context);
    coverageResultImporter.execute(context);

    if (!context.config().hasKey("sonar.tests")) {
      detectUndeclaredTestCases(context);
    }
  }

  private static void detectUndeclaredTestCases(SensorContext context) {
    FileSystem fs = context.fileSystem();
    Iterator<InputFile> inputFiles = fs.inputFiles(
      fs.predicates().and(fs.predicates().hasLanguage(Php.KEY), PHPUNIT_TEST_FILE_PREDICATE)).iterator();

    if (inputFiles.hasNext()) {
      LOG.warn("PHPUnit test cases are detected. Make sure to specify test sources via `sonar.test` to get more precise analysis results.");
    }
    while (inputFiles.hasNext()) {
      LOG.debug("Detected and undeclared test case in: {}", inputFiles.next().uri());
    }
  }

  static class TestCaseFilePredicate implements FilePredicate {

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String PHPUNIT_TEST_CASE_FQN = "PHPUnit\\Framework\\TestCase";

    @Override
    public boolean apply(InputFile inputFile) {
      try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
        // Only the first bytes are read to avoid slow execution for large single-line files
        byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
        String text = new String(bytes, inputFile.charset());
        return text.contains(PHPUNIT_TEST_CASE_FQN);
      } catch (IOException e) {
        // ignore file
        LOG.debug("Can not read file: {}", inputFile.uri());
        return false;
      }
    }
  }

}
