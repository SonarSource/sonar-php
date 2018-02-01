/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.phpunit.TestFileReport;

public final class TestSuite {

  private static final Logger LOGGER = Loggers.get(TestSuite.class);

  private String name;

  private String file;

  private double time;

  private List<TestSuite> testSuites = new ArrayList<>();

  @VisibleForTesting
  List<TestCase> testCases;

  public TestSuite(@Nullable String name, @Nullable String file, double time, List<TestCase> testCases) {
    this.name = name;
    this.file = file;
    this.time = time;
    this.testCases = testCases;
  }

  public TestSuite(String file) {
    this(null, file, 0, Collections.emptyList());
  }

  public Collection<TestFileReport> generateReports() {
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
   * @see TestFileReport
   *
   * @return true if the suite contains a file attribute
   */
  private boolean isFileBased() {
    return file != null;
  }

  private TestFileReport createReport() {
    final TestFileReport report = new TestFileReport(file, time);
    collectTestCases(report);
    return report;
  }

  private void collectTestCases(TestFileReport fileReport) {
    testCases.forEach(fileReport::addTestCase);
    testSuites.forEach(childSuite -> childSuite.collectTestCases(fileReport));
  }

  public void addNested(TestSuite child) {
    testSuites.add(child);
  }
}
