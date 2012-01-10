/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

import com.thoughtworks.xstream.XStream;

/**
 * The Class PhpUnitResultParser.
 */
public class PhpUnitResultParser implements BatchExtension {

  private static final double PERCENT = 100d;

  private static final double MILLISECONDS = 1000d;

  private static final int PRECISION = 1;

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitResultParser.class);

  /** The context. */
  private SensorContext context;

  /** The project. */
  private Project project;

  /**
   * Instantiates a new php unit result parser.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   */
  public PhpUnitResultParser(Project project, SensorContext context) {
    super();
    this.project = project;
    this.context = context;
  }

  /**
   * Gets the test suites.
   * 
   * @param report
   *          the report
   * @return the test suites
   */
  protected TestSuites getTestSuites(File report) {
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      // Sonar 2.2 migration
      xstream.setClassLoader(getClass().getClassLoader());
      xstream.aliasSystemAttribute("fileName", "class");
      xstream.processAnnotations(TestSuites.class);
      xstream.processAnnotations(TestSuite.class);
      xstream.processAnnotations(TestCase.class);
      inputStream = new FileInputStream(report);
      TestSuites testSuites = (TestSuites) xstream.fromXML(inputStream);
      LOG.debug("Tests suites: " + testSuites);
      return testSuites;
    } catch (IOException e) {
      throw new SonarException("Can't read PhpUnit report : " + report.getAbsolutePath(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Gets the php file pointed by the report.
   * 
   * @param report
   *          the unit test report
   * @param project
   *          the project
   * @return Resource<?> the file pointed by the report
   */
  private Resource<?> getUnitTestResource(PhpUnitTestReport report) {
    return org.sonar.api.resources.File.fromIOFile(new File(report.getFile()), project.getFileSystem().getTestDirs());
  }

  /**
   * Insert zero when no reports can be found.
   * 
   * @param project
   *          the analyzed project
   * @param context
   *          the execution context
   */
  private void insertZeroWhenNoReports() {
    context.saveMeasure(CoreMetrics.TESTS, 0.0);
  }

  /**
   * Collect the metrics found.
   * 
   * @param reportFile
   *          the reports directories to be scan
   */
  protected void parse(File reportFile) {
    if (reportFile == null) {
      insertZeroWhenNoReports();
    } else {
      LOG.info("Parsing file: " + reportFile.getAbsolutePath());
      parseFile(reportFile);
    }
  }

  /**
   * Parses the report file.
   * 
   * @param context
   *          the execution context
   * @param report
   *          the report file
   */
  private void parseFile(File report) {
    TestSuites testSuites = getTestSuites(report);
    List<PhpUnitTestReport> fileReports = readSuites(testSuites);
    for (PhpUnitTestReport fileReport : fileReports) {
      saveTestReportMeasures(fileReport);
    }
  }

  /**
   * Launches {@see PhpTestSuiteReader#readSuite(TestSuite)} for all its descendants.
   * 
   * @param testSuites
   *          the test suites
   * @return List<PhpUnitTestReport> A list of all test reports
   */
  private List<PhpUnitTestReport> readSuites(TestSuites testSuites) {
    List<PhpUnitTestReport> result = new ArrayList<PhpUnitTestReport>();
    for (TestSuite testSuite : testSuites.getTestSuites()) {
      PhpTestSuiteReader reader = new PhpTestSuiteReader();
      reader.readSuite(testSuite, null);
      result.addAll(reader.getReportsPerClass());
    }
    return result;
  }

  /**
   * Saves the measures contained in the test report.
   * 
   * @param context
   *          the execution context
   * @param project
   *          the analyzed project
   * @param fileReport
   *          the unit test report
   */
  protected void saveTestReportMeasures(PhpUnitTestReport fileReport) {
    if (!fileReport.isValid()) {
      return;
    }
    Resource<?> unitTestResource = getUnitTestResource(fileReport);
    if (unitTestResource != null) {// fileReport.getTests() > 0) {
      double testsCount = fileReport.getTests() - fileReport.getSkipped();
      if (fileReport.getSkipped() > 0) {
        context.saveMeasure(unitTestResource, CoreMetrics.SKIPPED_TESTS, (double) fileReport.getSkipped());
      }
      double duration = Math.round(fileReport.getTime() * MILLISECONDS);
      context.saveMeasure(unitTestResource, CoreMetrics.TEST_EXECUTION_TIME, duration);
      context.saveMeasure(unitTestResource, CoreMetrics.TESTS, testsCount);
      context.saveMeasure(unitTestResource, CoreMetrics.TEST_ERRORS, (double) fileReport.getErrors());
      context.saveMeasure(unitTestResource, CoreMetrics.TEST_FAILURES, (double) fileReport.getFailures());
      if (testsCount > 0) {
        double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
        double percentage = passedTests * PERCENT / testsCount;
        context.saveMeasure(unitTestResource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
      }
      saveTestsDetails(fileReport);
    } else {
      LOG.debug("Following file is not located in the test folder specified in the Sonar configuration: " + fileReport.getFile()
        + ". The test results won't be reported in Sonar.");
    }
  }

  /**
   * Save tests details.
   * 
   * @param context
   *          the context
   * @param fileReport
   *          the file report
   */
  private void saveTestsDetails(PhpUnitTestReport fileReport) {
    StringBuilder details = new StringBuilder();
    details.append("<tests-details>");
    for (TestCase detail : fileReport.getDetails()) {
      double time = ParsingUtils.scaleValue(detail.getTime() * MILLISECONDS, PRECISION);
      details.append("<testcase status=\"").append(detail.getStatus()).append("\" time=\"");
      details.append(time).append("\" name=\"").append(detail.getName().replaceAll(" ", "_")).append("\"");
      boolean isError = TestCase.STATUS_ERROR.equals(detail.getStatus());
      if (isError || TestCase.STATUS_FAILURE.equals(detail.getStatus())) {
        details.append(">").append(isError ? "<error message=\"" : "<failure message=\"");
        details.append(StringEscapeUtils.escapeXml(detail.getErrorMessage())).append("\"><![CDATA[");
        details.append(StringEscapeUtils.escapeXml(detail.getStackTrace())).append("]]>");
        details.append(isError ? "</error>" : "</failure>").append("</testcase>");
      } else {
        details.append("/>");
      }
    }
    details.append("</tests-details>");
    Resource<?> unitTestResource = getUnitTestResource(fileReport);
    if (unitTestResource != null) {
      context.saveMeasure(unitTestResource, new Measure(CoreMetrics.TEST_DATA, details.toString()));
    }
  }
}
