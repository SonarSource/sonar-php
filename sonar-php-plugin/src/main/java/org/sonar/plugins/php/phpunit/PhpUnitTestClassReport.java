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

public class PhpUnitTestClassReport {

  static final double PERCENT = 100d;
  static final double MILLISECONDS = 1000d;
  static final Logger THE_LOGGER = LoggerFactory.getLogger(PhpUnitResultImporter.class);
  private String classKey;
  private List<TestCase> details;
  private int errors = 0;
  private int failures = 0;
  private String file;
  private int skipped = 0;
  private int tests = 0;
  private double time = 0;

  public List<TestCase> getDetails() {
    return details;
  }

  public int getErrors() {
    return errors;
  }

  public int getFailures() {
    return failures;
  }

  public String getFile() {
    return file;
  }

  public int getSkipped() {
    return skipped;
  }

  public int getTests() {
    return tests;
  }

  public double getTime() {
    return time;
  }

  public boolean isValid() {
    return classKey != null;
  }

  public void setClassKey(String classKey) {
    this.classKey = classKey;
  }

  public void setDetails(List<TestCase> details) {
    this.details = details;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }

  public void setFailures(int failures) {
    this.failures = failures;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public void setSkipped(int skipped) {
    this.skipped = skipped;
  }

  public void setTests(int tests) {
    this.tests = tests;
  }

  public void setTime(double time) {
    this.time = time;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("classKey", classKey);
    builder.append("details", details);
    builder.append("errors", errors);
    builder.append("failures", failures);
    builder.append("file", file);
    builder.append("skipped", skipped);
    builder.append("tests", tests);
    builder.append("time", time);
    return builder.toString();
  }

  InputFile getUnitTestInputFile(FileSystem fileSystem) {
    FilePredicates predicates = fileSystem.predicates();
    return fileSystem.inputFile(predicates.and(
      predicates.hasPath(getFile()),
      predicates.hasType(InputFile.Type.TEST),
      predicates.hasLanguage(Php.KEY)));
  }

  void saveTestReportMeasures(SensorContext context, FileSystem fileSystem) {
    if (!isValid()) {
      return;
    }
    InputFile unitTestFile = getUnitTestInputFile(fileSystem);
    if (unitTestFile != null) {
      double testsCount = (double) getTests() - getSkipped();
      if (getSkipped() > 0) {
        context.<Integer>newMeasure().on(unitTestFile).withValue(getSkipped()).forMetric(CoreMetrics.SKIPPED_TESTS).save();
      }
      double duration = Math.round(getTime() * MILLISECONDS);

      context.<Long>newMeasure().on(unitTestFile).withValue((long) duration).forMetric(CoreMetrics.TEST_EXECUTION_TIME).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue((int) testsCount).forMetric(CoreMetrics.TESTS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(getErrors()).forMetric(CoreMetrics.TEST_ERRORS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(getFailures()).forMetric(CoreMetrics.TEST_FAILURES).save();
      if (testsCount > 0) {
        double passedTests = testsCount - getErrors() - getFailures();
        double percentage = passedTests * PERCENT / testsCount;
        context.<Double>newMeasure().on(unitTestFile).withValue(ParsingUtils.scaleValue(percentage)).forMetric(CoreMetrics.TEST_SUCCESS_DENSITY).save();
      }

    } else {
      THE_LOGGER.debug("Following file is not located in the test folder specified in the Sonar configuration: " + getFile()
        + ". The test results won't be reported in Sonar.");
    }
  }
}
