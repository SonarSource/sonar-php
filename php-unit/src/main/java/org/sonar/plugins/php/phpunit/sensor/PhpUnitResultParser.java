/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpunit.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.resources.PhpFile;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

import com.thoughtworks.xstream.XStream;

/**
 * The Class PhpUnitResultParser.
 */
public class PhpUnitResultParser {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PhpUnitResultParser.class);

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
  private TestSuites getTestSuites(File report) {
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      // xstream.aliasSystemAttribute("classType", "class");
      xstream.aliasSystemAttribute("className", "class");
      xstream.processAnnotations(TestSuites.class);
      xstream.processAnnotations(TestSuite.class);
      xstream.processAnnotations(TestCase.class);
      inputStream = new FileInputStream(report);
      TestSuites testSuites = (TestSuites) xstream.fromXML(inputStream);
      Log.debug("Tests suites: " + testSuites);
      return testSuites;
    } catch (IOException e) {
      throw new SonarException("Can't read pUnit report : " + report.getName(), e);
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
   * @return PhpFile pointed by the report
   */
  private PhpFile getUnitTestResource(PhpUnitTestReport report, Project project) {
    return PhpFile.fromAbsolutePath(report.getFile(), project.getFileSystem().getTestDirs(), true);
  }

  /**
   * Insert zero when no reports can be found.
   * 
   * @param project
   *          the analyzed project
   * @param context
   *          the execution context
   */
  private void insertZeroWhenNoReports(Project project, SensorContext context) {
    if ( !"pom".equalsIgnoreCase(project.getPom().getPackaging())) {
      context.saveMeasure(CoreMetrics.TESTS, 0.0);
    }
  }

  /**
   * Collect the metrics found.
   * 
   * @param reportFile
   *          the reports directories to be scan
   */
  protected void parse(File reportFile) {
    if (reportFile == null) {
      insertZeroWhenNoReports(project, context);
    } else {
      logger.info("Parsing file : {0}", reportFile.getName());
      parseFile(context, reportFile, project);
    }
  }

  /**
   * Parses the report file.
   * 
   * @param context
   *          the execution context
   * @param report
   *          the report file
   * @param project
   *          the project
   */
  private void parseFile(SensorContext context, File report, Project project) {
    TestSuites testSuites = getTestSuites(report);
    List<PhpUnitTestReport> fileReports = readSuites(testSuites);
    for (PhpUnitTestReport fileReport : fileReports) {
      saveTestReportMeasures(context, project, fileReport);
    }
  }

  /**
   * Launches {@see PhpTestSuiteReader#readSuite(TestSuite)} for all its descendants.
   * 
   * @param testSuites
   *          the test suites
   * @return List<PhpUnitTestReport> A list of all test reports
   */
  public List<PhpUnitTestReport> readSuites(TestSuites testSuites) {
    List<PhpUnitTestReport> result = new ArrayList<PhpUnitTestReport>();
    for (TestSuite testSuite : testSuites.getTestSuites()) {
      PhpTestSuiteReader reader = new PhpTestSuiteReader();
      List<PhpUnitTestReport> list = reader.readSuite(testSuite, null);
      result.addAll(list);
    }
    return result;
  }

  /**
   * Save class measure.
   * 
   * @param context
   *          the context
   * @param fileReport
   *          the file report
   * @param metric
   *          the metric
   * @param value
   *          the value
   * @param project
   *          the project
   */
  private void saveClassMeasure(SensorContext context, PhpUnitTestReport fileReport, Metric metric, double value, Project project) {
    if ( !Double.isNaN(value)) {
      context.saveMeasure(getUnitTestResource(fileReport, project), metric, value);
    }
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
  private void saveTestReportMeasures(SensorContext context, Project project, PhpUnitTestReport fileReport) {
    if ( !fileReport.isValid()) {
      return;
    }
    if (fileReport.getTests() > 0) {
      double testsCount = fileReport.getTests() - fileReport.getSkipped();
      if (fileReport.getSkipped() > 0) {
        saveClassMeasure(context, fileReport, CoreMetrics.SKIPPED_TESTS, fileReport.getSkipped(), project);
      }
      saveClassMeasure(context, fileReport, CoreMetrics.TESTS, testsCount, project);
      saveClassMeasure(context, fileReport, CoreMetrics.TEST_ERRORS, fileReport.getErrors(), project);
      saveClassMeasure(context, fileReport, CoreMetrics.TEST_FAILURES, fileReport.getFailures(), project);
      saveClassMeasure(context, fileReport, CoreMetrics.TEST_EXECUTION_TIME, fileReport.getTime(), project);
      double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
      if (testsCount > 0) {
        double percentage = passedTests * 100d / testsCount;
        saveClassMeasure(context, fileReport, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage), project);
      }
      saveTestsDetails(context, fileReport, project);
    }
  }

  /**
   * Save tests details.
   * 
   * @param context
   *          the context
   * @param fileReport
   *          the file report
   * @param project
   *          the project
   */
  private void saveTestsDetails(SensorContext context, PhpUnitTestReport fileReport, Project project) {
    StringBuilder testCaseDetails = new StringBuilder(256);
    testCaseDetails.append("<tests-details>");
    for (TestCase detail : fileReport.getDetails()) {
      testCaseDetails.append("<testcase status=\"").append(detail.getStatus()).append("\" time=\"").append(detail.getTime()).append(
          "\" name=\"").append(detail.getName().replaceAll(" ", "_")).append("\"");
      boolean isError = TestCase.STATUS_ERROR.equals(detail.getStatus());
      if (isError || TestCase.STATUS_FAILURE.equals(detail.getStatus())) {
        testCaseDetails.append(">").append(isError ? "<error message=\"" : "<failure message=\"").append(
            StringEscapeUtils.escapeXml(detail.getErrorMessage())).append("\">").append("<![CDATA[").append(
            StringEscapeUtils.escapeXml(detail.getStackTrace())).append("]]>").append(isError ? "</error>" : "</failure>").append(
            "</testcase>");
      } else {
        testCaseDetails.append("/>");
      }
    }
    testCaseDetails.append("</tests-details>");
    context.saveMeasure(getUnitTestResource(fileReport, project), new Measure(CoreMetrics.TEST_DATA, testCaseDetails.toString()));
  }

}
