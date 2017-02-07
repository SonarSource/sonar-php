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

import java.util.List;

/**
 * The Class TestSuite.
 */
@XStreamAlias("testsuite")
public final class TestSuite {

  /**
   * The name.
   */
  @XStreamAsAttribute
  private String name;

  /**
   * The file.
   */
  @XStreamAsAttribute
  private String file;

  /**
   * The tests.
   */
  @XStreamAsAttribute
  private String tests;

  /**
   * The assertions.
   */
  @XStreamAsAttribute
  private String assertions;

  /**
   * The time.
   */
  @XStreamAsAttribute
  private double time;

  /**
   * The test suites.
   */
  @XStreamImplicit(itemFieldName = "testsuite")
  private List<TestSuite> testSuites;

  /**
   * The test cases.
   */
  @XStreamImplicit(itemFieldName = "testcase")
  private List<TestCase> testCases;

  /**
   * Empty constructor is required by xstream in order to
   * be compatible with Java 7.
   * */
  public TestSuite() {
    // Empty constructor is required by xstream
  }

  /**
   * Instantiates a new test suite.
   *
   * @param name       the name
   * @param file       the file
   * @param tests      the tests
   * @param assertions the assertions
   * @param time       the time
   * @param testSuites the test suites
   * @param testCases  the test cases
   */
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

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets the file.
   *
   * @return the file
   */
  public String getFile() {
    return file;
  }

  /**
   * Sets the file.
   *
   * @param file the new file
   */
  public void setFile(final String file) {
    this.file = file;
  }

  /**
   * Gets the tests.
   *
   * @return the tests
   */
  public String getTests() {
    return tests;
  }

  /**
   * Sets the tests.
   *
   * @param tests the new tests
   */
  public void setTests(final String tests) {
    this.tests = tests;
  }

  /**
   * Gets the assertions.
   *
   * @return the assertions
   */
  public String getAssertions() {
    return assertions;
  }

  /**
   * Sets the assertions.
   *
   * @param assertions the new assertions
   */
  public void setAssertions(final String assertions) {
    this.assertions = assertions;
  }

  /**
   * Gets the time.
   *
   * @return the time
   */
  public double getTime() {
    return time;
  }

  /**
   * Gets the test suites.
   *
   * @return the test suites
   */
  public List<TestSuite> getTestSuites() {
    return testSuites;
  }

  /**
   * Gets the test cases.
   *
   * @return the test cases
   */
  public List<TestCase> getTestCases() {
    return testCases;
  }
}
