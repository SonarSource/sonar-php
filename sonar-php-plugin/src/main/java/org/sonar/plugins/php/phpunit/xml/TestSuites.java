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
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.php.phpunit.PhpTestSuiteReader;
import org.sonar.plugins.php.phpunit.PhpUnitTestClassReport;

@XStreamAlias("testsuites")
public final class TestSuites {

  @XStreamImplicit(itemFieldName = "testsuite")
  private List<TestSuite> testSuiteList = new ArrayList<>();

  /**
   * Empty constructor is required by xstream in order to
   * be compatible with Java 7.
   * */
  public TestSuites() {
    // Empty constructor is required by xstream
  }

  public List<TestSuite> getTestSuiteList() {
    return testSuiteList;
  }

  public void setTestSuiteList(final List<TestSuite> testSuiteList) {
    this.testSuiteList = testSuiteList;
  }

  public void addTestSuite(final TestSuite testSuite) {
    testSuiteList.add(testSuite);
  }

  public List<PhpUnitTestClassReport> arrangeSuitesIntoTestClassReports() {
    List<PhpUnitTestClassReport> result = new ArrayList<>();
    for (TestSuite testSuite : getTestSuiteList()) {
      PhpTestSuiteReader reader = new PhpTestSuiteReader();
      reader.readSuite(testSuite, null);
      result.addAll(reader.getReportsPerClass());
    }
    return result;
  }
}
