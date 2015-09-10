/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TestSuites.
 */
@XStreamAlias("testsuites")
public final class TestSuites {

  /**
   * The test suites.
   */
  @XStreamImplicit(itemFieldName = "testsuite")
  private List<TestSuite> testSuites;

  /**
   * Empty constructor is required by xstream in order to
   * be compatible with Java 7.
   * */
  public TestSuites() {
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
   * @param testSuites the new test suites
   */
  public void setTestSuites(final List<TestSuite> testSuites) {
    this.testSuites = testSuites;
  }

  /**
   * Adds the test suite.
   *
   * @param testSuite the test suite
   */
  public void addTestSuite(final TestSuite testSuite) {
    if (testSuites == null) {
      testSuites = new ArrayList<TestSuite>();
    }
    testSuites.add(testSuite);
  }
}
