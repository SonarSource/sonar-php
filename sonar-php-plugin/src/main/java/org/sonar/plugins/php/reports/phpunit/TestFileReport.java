/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.function.Consumer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.reports.ExternalReportFileHandler;
import org.sonar.plugins.php.reports.phpunit.xml.TestCase;

/**
 * The PhpUnitTestFileReport contains all the results of test cases appearing in a given test file.
 * The reason why the report is file-based (as opposed to class-based) is that the SonarQube measures
 * are stored per file.
 */
public class TestFileReport {

  private int errors = 0;
  private int failures = 0;
  private String file;
  private int skipped = 0;
  private int tests = 0;
  private double testDuration = 0;

  public TestFileReport(String file, double testDuration) {
    this.file = file;
    this.testDuration = testDuration;
  }

  public void saveTestMeasures(SensorContext context, ExternalReportFileHandler fileHandler, Consumer<String> addUnresolvedInputFiles) {
    file = fileHandler.relativePath(file);
    InputFile unitTestFile = getUnitTestInputFile(context.fileSystem());
    if (unitTestFile != null) {
      context.<Integer>newMeasure().on(unitTestFile).withValue(skipped).forMetric(CoreMetrics.SKIPPED_TESTS).save();

      context.<Long>newMeasure().on(unitTestFile).withValue((long) testDurationMilliseconds()).forMetric(CoreMetrics.TEST_EXECUTION_TIME).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue((int) liveTests()).forMetric(CoreMetrics.TESTS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(errors).forMetric(CoreMetrics.TEST_ERRORS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(failures).forMetric(CoreMetrics.TEST_FAILURES).save();
    } else {
      addUnresolvedInputFiles.accept(file);
    }
  }

  private double liveTests() {
    return (double) tests - skipped;
  }

  public double testDurationMilliseconds() {
    return testDuration * 1000d;
  }

  private InputFile getUnitTestInputFile(FileSystem fileSystem) {
    FilePredicates predicates = fileSystem.predicates();
    return fileSystem.inputFile(predicates.and(
      predicates.hasPath(file),
      predicates.hasType(InputFile.Type.TEST),
      predicates.hasLanguage(Php.KEY)));
  }

  public void addTestCase(TestCase testCase) {
    if (testCase.getStatus() == TestCase.Status.SKIPPED) {
      this.skipped++;
    } else if (testCase.getStatus() == TestCase.Status.FAILURE) {
      this.failures++;
    } else if (testCase.getStatus() == TestCase.Status.ERROR) {
      this.errors++;
    }
    this.tests++;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestFileReport that = (TestFileReport) o;

    return new EqualsBuilder()
      .append(errors, that.errors)
      .append(failures, that.failures)
      .append(skipped, that.skipped)
      .append(tests, that.tests)
      .append(testDuration, that.testDuration)
      .append(file, that.file)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(errors)
      .append(failures)
      .append(file)
      .append(skipped)
      .append(tests)
      .append(testDuration)
      .toHashCode();
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("errors", errors);
    builder.append("failures", failures);
    builder.append("file", file);
    builder.append("skipped", skipped);
    builder.append("tests", tests);
    builder.append("testDuration", testDuration);
    return builder.toString();
  }

  public int getTests() {
    return tests;
  }
}
