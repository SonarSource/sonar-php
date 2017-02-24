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
package org.sonar.plugins.php.phpunit.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.php.phpunit.PhpUnitTestFileReport;

@XStreamAlias("testsuite")
public final class TestSuite {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);

  @XStreamAsAttribute
  private String name;

  @XStreamAsAttribute
  private String file;

  @XStreamAsAttribute
  private String tests;

  @XStreamAsAttribute
  private String assertions;

  @XStreamAsAttribute
  private double time;

  @XStreamImplicit(itemFieldName = "testsuite")
  private List<TestSuite> testSuites;

  @XStreamImplicit(itemFieldName = "testcase")
  private List<TestCase> testCases;

  /**
   * Empty constructor is required by xstream in order to
   * be compatible with Java 7.
   * */
  public TestSuite() {
    // Empty constructor is required by xstream
    this.testSuites = new ArrayList<>();
    this.testCases = new ArrayList<>();
  }

  public TestSuite(final String name, final String file, final String tests, final String assertions,
    final double time, final List<TestSuite> testSuites, final List<TestCase> testCases) {
    super();
    this.name = name;
    this.file = file;
    this.tests = tests;
    this.assertions = assertions;
    this.time = time;
    this.testSuites = testSuites;
    this.testCases = testCases;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getFile() {
    return file;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  public String getAssertions() {
    return assertions;
  }

  public void setAssertions(final String assertions) {
    this.assertions = assertions;
  }

  public double getTime() {
    return time;
  }

  public List<TestCase> getTestCases() {
    return testCases;
  }

  public boolean isFileBasedSuite() {
    return getFile() != null;
  }

  /**
   * Reads the given test suite.
   * <p/>
   * Due to a inconsistent XML format in phpUnit, we have to importReport enclosing testsuite name for generated testcases when a testcase holds
   * the annotation dataProvider.
   *
   * @param activeReport
   */
  public Collection<PhpUnitTestFileReport> generateReports(@Nullable PhpUnitTestFileReport activeReport) {
    Map<String, PhpUnitTestFileReport> reportsPerFile = new HashMap<>();
    if (isFileBasedSuite()) {
      reportsPerFile.putIfAbsent(getFile(), new PhpUnitTestFileReport(getFile(), getTime()));
      activeReport = reportsPerFile.get(getFile());
    }
    for (TestCase testCase : getTestCases()) {
      if (activeReport != null) {
        activeReport.addTestCase(testCase);
      } else {
        LOGGER.warn("Test cases must always be descendants of a file-based suite, skipping : " + testCase.fullName() + " in " + name);
      }
    }
    for (TestSuite childSuite : this.testSuites) {
      childSuite.generateReports(activeReport);
    }
    return reportsPerFile.values();
  }
}
