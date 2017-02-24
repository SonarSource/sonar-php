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

@XStreamAlias("testsuite")
public final class TestSuite {

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

  public String getName() {
    return name;
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

  public String getTests() {
    return tests;
  }

  public void setTests(final String tests) {
    this.tests = tests;
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

  public List<TestSuite> getTestSuites() {
    return testSuites;
  }

  public List<TestCase> getTestCases() {
    return testCases;
  }
}
