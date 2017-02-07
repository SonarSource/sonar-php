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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.plugins.php.phpunit.xml.TestSuite;

/**
 * The PhpTestSuiteParser .
 */
public class PhpTestSuiteReader {

  /**
   */
  private static final String TESTSUITE_CLASS_NAME_SEPARATOR = "::";
  /**
   * The reports per class.
   */
  private Map<String, PhpUnitTestReport> reportsPerClass = new HashMap<>();

  /**
   * Cumulates test case details.
   *
   * @param testCase the test case to analyse
   * @param report   the report in which results will be added
   */
  private static void cumulateTestCaseDetails(TestCase testCase, PhpUnitTestReport report) {

    if (TestCase.STATUS_SKIPPED.equals(testCase.getStatus())) {
      report.setSkipped(report.getSkipped() + 1);
    } else if (TestCase.STATUS_FAILURE.equals(testCase.getStatus())) {
      report.setFailures(report.getFailures() + 1);
    } else if (TestCase.STATUS_ERROR.equals(testCase.getStatus())) {
      report.setErrors(report.getErrors() + 1);
    }
    report.setTests(report.getTests() + 1);
    report.getDetails().add(testCase);
  }

  /**
   * Reads the given test suite.
   * <p/>
   * Due to a inconsistent XML format in phpUnit, we have to parse enclosing testsuite name for generated testcases when a testcase holds
   * the annotation dataProvider.
   *
   * @param testSuite the test suite
   * Method adds to the field <code>reportsPerClass</code> reports per php class
   */
  public void readSuite(TestSuite testSuite, @Nullable String parentFileName) {
    List<TestSuite> testSuites = testSuite.getTestSuites();
    if (testSuites != null) {
      for (TestSuite childSuite : testSuites) {
        readSuite(childSuite, testSuite.getFile());
      }
    }
    // For all cases
    List<TestCase> testCases = testSuite.getTestCases();
    if (testCases != null) {
      readTestCases(testSuite, parentFileName, testCases);
    }
  }

  private void readTestCases(TestSuite testSuite, @Nullable String parentFileName, List<TestCase> testCases) {
    for (TestCase testCase : testCases) {
      String testClassName = testCase.getClassName();
      // For test cases with @dataProvider. we get the fileName in the enclosing testSuite in the name attribute before string "::"
      if (testClassName == null) {
        testClassName = StringUtils.substringBefore(testSuite.getName(), TESTSUITE_CLASS_NAME_SEPARATOR);
      }
      PhpUnitTestReport report = reportsPerClass.get(testClassName);
      // If no reports exists for this class we create one
      if (report == null) {
        report = new PhpUnitTestReport();
        report.setDetails(new ArrayList<TestCase>());
        report.setClassKey(testClassName);

        String file = testCase.getFile();
        // test cases with @dataProvider, we get the file name in the parent test suite.
        if (file == null) {
          file = parentFileName;
        }

        if (file != null) {
          report.setFile(file);
          reportsPerClass.put(testClassName, report);
        }
      }
      if (parentFileName == null) {
        report.setTime(testSuite.getTime());
      }
      cumulateTestCaseDetails(testCase, report);
    }
  }

  /**
   * Returns the collection of test results for the suite that has been analyzed.
   *
   * @return the collection of {@link PhpUnitTestReport} objects
   */
  public Collection<PhpUnitTestReport> getReportsPerClass() {
    return reportsPerClass.values();
  }

}
