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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

@BatchSide
@ExtensionPoint
public class PhpUnitResultParser implements PhpUnitParser {

  private static final double PERCENT = 100d;

  private static final double MILLISECONDS = 1000d;

  private static final int PRECISION = 1;

  private static final Logger THE_LOGGER = LoggerFactory.getLogger(PhpUnitResultParser.class);

  private FileSystem fileSystem;

  private FilePredicates filePredicates;

  public PhpUnitResultParser(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.filePredicates = fileSystem.predicates();
  }

  @Override
  public void parse(File reportFile, SensorContext context, Map<String, Integer> numberOfLinesOfCode) {
    Preconditions.checkNotNull(reportFile);
    THE_LOGGER.debug("Parsing file: " + reportFile.getAbsolutePath());
    TestSuites testSuites = getTestSuites(reportFile);
    List<PhpUnitTestReport> fileReports = readSuites(testSuites);
    for (PhpUnitTestReport fileReport : Lists.reverse(fileReports)) {
      saveTestReportMeasures(fileReport, context);
    }
  }

  TestSuites getTestSuites(File report) {
    try (InputStream inputStream = new FileInputStream(report)) {
      XStream xstream = getXStream();
      TestSuites testSuites = (TestSuites) xstream.fromXML(inputStream);
      THE_LOGGER.debug("Tests suites: " + testSuites.getTestSuiteList());
      return testSuites;
    } catch (IOException e) {
      throw new IllegalStateException("Can't read PhpUnit report : " + report.getAbsolutePath(), e);
    }
  }

  private static List<PhpUnitTestReport> readSuites(TestSuites testSuites) {
    List<PhpUnitTestReport> result = new ArrayList<>();
    for (TestSuite testSuite : testSuites.getTestSuiteList()) {
      PhpTestSuiteReader reader = new PhpTestSuiteReader();
      reader.readSuite(testSuite, null);
      result.addAll(reader.getReportsPerClass());
    }
    return result;
  }

  private void saveTestReportMeasures(PhpUnitTestReport fileReport, SensorContext context) {
    if (!fileReport.isValid()) {
      return;
    }
    InputFile unitTestFile = getUnitTestInputFile(fileReport);
    if (unitTestFile != null) {
      double testsCount = (double) fileReport.getTests() - fileReport.getSkipped();
      if (fileReport.getSkipped() > 0) {
        context.<Integer>newMeasure().on(unitTestFile).withValue(fileReport.getSkipped()).forMetric(CoreMetrics.SKIPPED_TESTS).save();
      }
      double duration = Math.round(fileReport.getTime() * MILLISECONDS);

      context.<Long>newMeasure().on(unitTestFile).withValue((long) duration).forMetric(CoreMetrics.TEST_EXECUTION_TIME).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue((int) testsCount).forMetric(CoreMetrics.TESTS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(fileReport.getErrors()).forMetric(CoreMetrics.TEST_ERRORS).save();
      context.<Integer>newMeasure().on(unitTestFile).withValue(fileReport.getFailures()).forMetric(CoreMetrics.TEST_FAILURES).save();
      if (testsCount > 0) {
        double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
        double percentage = passedTests * PERCENT / testsCount;
        context.<Double>newMeasure().on(unitTestFile).withValue(ParsingUtils.scaleValue(percentage)).forMetric(CoreMetrics.TEST_SUCCESS_DENSITY).save();
      }

    } else {
      THE_LOGGER.debug("Following file is not located in the test folder specified in the Sonar configuration: " + fileReport.getFile()
        + ". The test results won't be reported in Sonar.");
    }
  }

  private XStream getXStream() {
    XStream xstream = new XStream() {
      // Trick to ignore unknown elements
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
          @Override
          public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            return definedIn != Object.class && super.shouldSerializeMember(definedIn, fieldName);
          }
        };
      }
    };
    xstream.setClassLoader(getClass().getClassLoader());
    xstream.aliasSystemAttribute("fileName", "class");
    xstream.processAnnotations(TestSuites.class);
    xstream.processAnnotations(TestSuite.class);
    xstream.processAnnotations(TestCase.class);
    return xstream;
  }

  private InputFile getUnitTestInputFile(PhpUnitTestReport report) {
    return fileSystem.inputFile(fileSystem.predicates().and(
      filePredicates.hasPath(report.getFile()),
      filePredicates.hasType(InputFile.Type.TEST),
      filePredicates.hasLanguage(Php.KEY)));
  }

}
