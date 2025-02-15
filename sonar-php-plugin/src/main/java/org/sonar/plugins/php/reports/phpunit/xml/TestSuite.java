/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports.phpunit.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.php.reports.phpunit.TestFileReport;

public final class TestSuite {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestSuite.class);

  private String name;

  private String file;

  private double time;

  private List<TestSuite> testSuites = new ArrayList<>();

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
    testCases.forEach(testCase -> LOGGER.warn("Test cases must always be descendants of a file-based suite, skipping : {} in {}", testCase.fullName(), name));
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
