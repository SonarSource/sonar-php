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
package org.sonar.plugins.php.phpunit.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The Class TestSuite.
 */
@XStreamAlias("testsuite")
public final class TestSuite {

  /** The name. */
  @XStreamAsAttribute
  private String name;

  /** The file. */
  @XStreamAsAttribute
  private String file;

  /** The full package. */
  @XStreamAsAttribute
  private String fullPackage;

  /** The category. */
  @XStreamAsAttribute
  private String category;

  /** The package name. */
  @XStreamAsAttribute
  @XStreamAlias("package")
  private String packageName;

  /** The subpackage. */
  @XStreamAsAttribute
  private String subpackage;

  /** The tests. */
  @XStreamAsAttribute
  private String tests;

  /** The assertions. */
  @XStreamAsAttribute
  private String assertions;

  /** The failures. */
  @XStreamAsAttribute
  private int failures;

  /** The errors. */
  @XStreamAsAttribute
  private int errors;

  /** The time. */
  @XStreamAsAttribute
  private double time;

  /** The test suites. */
  @XStreamImplicit(itemFieldName = "testsuite")
  private List<TestSuite> testSuites;

  /** The test cases. */
  @XStreamImplicit(itemFieldName = "testcase")
  private List<TestCase> testCases;

  /**
   * Instantiates a new test suite.
   * 
   * @param name
   *          the name
   * @param file
   *          the file
   * @param fullPackage
   *          the full package
   * @param category
   *          the category
   * @param packageName
   *          the package name
   * @param subpackage
   *          the subpackage
   * @param tests
   *          the tests
   * @param assertions
   *          the assertions
   * @param failures
   *          the failures
   * @param errors
   *          the errors
   * @param time
   *          the time
   * @param testSuites
   *          the test suites
   * @param testCases
   *          the test cases
   */
  public TestSuite(final String name, final String file, final String fullPackage, final String category, final String packageName,
      final String subpackage, final String tests, final String assertions, final Integer failures, final Integer errors,
      final double time, final List<TestSuite> testSuites, final List<TestCase> testCases) {
    super();
    this.name = name;
    this.file = file;
    this.fullPackage = fullPackage;
    this.category = category;
    this.packageName = packageName;
    this.subpackage = subpackage;
    this.tests = tests;
    this.assertions = assertions;
    this.failures = failures;
    this.errors = errors;
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
   * @param name
   *          the new name
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
   * @param file
   *          the new file
   */
  public void setFile(final String file) {
    this.file = file;
  }

  /**
   * Gets the full package.
   * 
   * @return the full package
   */
  public String getFullPackage() {
    return fullPackage;
  }

  /**
   * Sets the full package.
   * 
   * @param fullPackage
   *          the new full package
   */
  public void setFullPackage(final String fullPackage) {
    this.fullPackage = fullPackage;
  }

  /**
   * Gets the category.
   * 
   * @return the category
   */
  public String getCategory() {
    return category;
  }

  /**
   * Sets the category.
   * 
   * @param category
   *          the new category
   */
  public void setCategory(final String category) {
    this.category = category;
  }

  /**
   * Gets the package name.
   * 
   * @return the package name
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Sets the package name.
   * 
   * @param packageName
   *          the new package name
   */
  public void setPackageName(final String packageName) {
    this.packageName = packageName;
  }

  /**
   * Gets the subpackage.
   * 
   * @return the subpackage
   */
  public String getSubpackage() {
    return subpackage;
  }

  /**
   * Sets the subpackage.
   * 
   * @param subpackage
   *          the new subpackage
   */
  public void setSubpackage(final String subpackage) {
    this.subpackage = subpackage;
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
   * @param tests
   *          the new tests
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
   * @param assertions
   *          the new assertions
   */
  public void setAssertions(final String assertions) {
    this.assertions = assertions;
  }

  /**
   * Gets the failures.
   * 
   * @return the failures
   */
  public Integer getFailures() {
    return failures;
  }

  /**
   * Sets the failures.
   * 
   * @param failures
   *          the new failures
   */
  public void setFailures(final Integer failures) {
    this.failures = failures;
  }

  /**
   * Gets the errors.
   * 
   * @return the errors
   */
  public Integer getErrors() {
    return errors;
  }

  /**
   * Sets the errors.
   * 
   * @param errors
   *          the new errors
   */
  public void setErrors(final Integer errors) {
    this.errors = errors;
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
   * Sets the time.
   * 
   * @param time
   *          the new time
   */
  public void setTime(final double time) {
    this.time = time;
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
   * Sets the test suites.
   * 
   * @param testSuites
   *          the new test suites
   */
  public void setTestSuites(final List<TestSuite> testSuites) {
    this.testSuites = testSuites;
  }

  /**
   * Gets the test cases.
   * 
   * @return the test cases
   */
  public List<TestCase> getTestCases() {
    return testCases;
  }

  /**
   * Sets the test cases.
   * 
   * @param testCases
   *          the new test cases
   */
  public void setTestCases(final List<TestCase> testCases) {
    this.testCases = testCases;
  }

}
