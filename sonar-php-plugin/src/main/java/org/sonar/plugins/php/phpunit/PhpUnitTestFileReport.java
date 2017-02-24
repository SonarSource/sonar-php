/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.util.ArrayList;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.TestCase;

import java.util.List;

public class PhpUnitTestFileReport {

  private static final double PERCENT = 100d;
  private static final double MILLISECONDS = 1000d;
  private static final Logger LOGGER = LoggerFactory.getLogger(PhpUnitTestResultImporter.class);
  private List<TestCase> details;
  private int errors = 0;
  private int failures = 0;
  private String file;
  private int skipped = 0;
  private int tests = 0;
  private double time = 0;

  public PhpUnitTestFileReport(String file, double time) {
    this.file = file;
    this.time = time;
    this.details = new ArrayList<>();
  }

  public List<TestCase> getDetails() {
    return details;
  }

  public void setDetails(List<TestCase> details) {
    this.details = details;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public void saveTestReportMeasures(SensorContext context, FileSystem fileSystem) {
    InputFile unitTestFile = getUnitTestInputFile(fileSystem);
    if (unitTestFile != null) {
      double testsCount = (double) tests - skipped;
      if (skipped > 0) {
        context.<Integer>newMeasure().on(unitTestFile).withValue(skipped).forMetric(CoreMetrics.SKIPPED_TESTS).save();
      }
      double duration = Math.round(time * MILLISECONDS);

      context.<Long>newMeasure().on(unitTestFile).withValue((long) duration).forMetric(CoreMetrics.TEST_EXECUTION_TIME).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue((int) testsCount).forMetric(CoreMetrics.TESTS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(errors).forMetric(CoreMetrics.TEST_ERRORS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(failures).forMetric(CoreMetrics.TEST_FAILURES).save();
      if (testsCount > 0) {
        double passedTests = testsCount - errors - failures;
        double percentage = passedTests * PERCENT / testsCount;
        context.<Double>newMeasure().on(unitTestFile).withValue(ParsingUtils.scaleValue(percentage)).forMetric(CoreMetrics.TEST_SUCCESS_DENSITY).save();
      }

    } else {
      LOGGER.debug("Following file is not located in the test folder specified in the Sonar configuration: " + file
        + ". The test results won't be reported in Sonar.");
    }
  }

  private InputFile getUnitTestInputFile(FileSystem fileSystem) {
    FilePredicates predicates = fileSystem.predicates();
    return fileSystem.inputFile(predicates.and(
      predicates.hasPath(file),
      predicates.hasType(InputFile.Type.TEST),
      predicates.hasLanguage(Php.KEY)));
  }

  public void addTestCase(TestCase testCase) {
    if (TestCase.STATUS_SKIPPED.equals(testCase.getStatus())) {
      this.skipped++;
    } else if (TestCase.STATUS_FAILURE.equals(testCase.getStatus())) {
      this.failures++;
    } else if (TestCase.STATUS_ERROR.equals(testCase.getStatus())) {
      this.errors++;
    }
    this.tests++;
    details.add(testCase);
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("details", details);
    builder.append("errors", errors);
    builder.append("failures", failures);
    builder.append("file", file);
    builder.append("skipped", skipped);
    builder.append("tests", tests);
    builder.append("time", time);
    return builder.toString();
  }
}
