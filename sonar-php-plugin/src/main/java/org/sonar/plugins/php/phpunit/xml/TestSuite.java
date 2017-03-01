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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.internal.google.common.annotations.VisibleForTesting;
import org.sonar.plugins.php.phpunit.PhpUnitTestFileReport;

@XStreamAlias("testsuite")
public final class TestSuite {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);

  @XStreamAsAttribute
  private String name;

  @XStreamAsAttribute
  private String file;

  @XStreamAsAttribute
  private double time;

  @XStreamImplicit(itemFieldName = "testsuite")
  private List<TestSuite> testSuites = new ArrayList<>();

  @VisibleForTesting
  @XStreamImplicit(itemFieldName = "testcase")
  List<TestCase> testCases = new ArrayList<>();

  /**
   * Empty constructor is required by xstream in order to
   * be compatible with Java 7.
   * */
  public TestSuite() {
    // Zero parameters constructor is required by xstream
  }

  @VisibleForTesting
  TestSuite(@Nullable String file, TestCase... testCases) {
    this.file = file;
    this.testCases = Arrays.asList(testCases);
  }

  public Collection<PhpUnitTestFileReport> generateReports() {
    return collectAllFileBasedSuites().stream().map(TestSuite::createReport).collect(Collectors.toSet());
  }

  private Collection<TestSuite> collectAllFileBasedSuites() {
    final Set<TestSuite> fileBasedTestSuites = new HashSet<>();
    if (this.isFileBased()) {
      fileBasedTestSuites.add(this);
    } else {
      logMisplacedTestCases();
    }
    testSuites.forEach(childSuite -> fileBasedTestSuites.addAll(childSuite.collectAllFileBasedSuites()));
    return fileBasedTestSuites;
  }

  private void logMisplacedTestCases() {
    testCases.forEach(testCase -> LOGGER.warn("Test cases must always be descendants of a file-based suite, skipping : " + testCase.fullName() + " in " + name));
  }

  /**
   * Four types of suites are known :
   * - file-based (a suite generated out of all the tests listed in a PHPUnit test class
   * - folder-based (a suite generated out of PHPUnit being run on a folder)
   * - configuration-based (a suite explicitly defined in the phpunit.xml configuration file)
   * - data-provider-based (a suite generated to contain all dataset variants of a test fed with a PHPUnit dataProvider)
   *
   * Currently we only care about distinguishing between the file-based suite and all the others.
   * @see PhpUnitTestFileReport
   *
   * @return true if the suite contains a file attribute
   */
  private boolean isFileBased() {
    return file != null;
  }

  private PhpUnitTestFileReport createReport() {
    final PhpUnitTestFileReport report = new PhpUnitTestFileReport(file, time);
    collectTestCases(report);
    return report;
  }

  private void collectTestCases(PhpUnitTestFileReport fileReport) {
    testCases.forEach(fileReport::addTestCase);
    testSuites.forEach(childSuite -> childSuite.collectTestCases(fileReport));
  }

  @VisibleForTesting
  void addNested(TestSuite child) {
    testSuites.add(child);
  }
}
