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
/**
 * @author gennadiyl
 */
package org.sonar.plugins.php.phpunit.xml;

import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.php.phpunit.PhpUnitTestFileReport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestSuitesTest {

  /**
   * Empty constructors are required by xstream for TestSuite and TestSuite, in order to
   * be compatible with Java 7.
   */
  @Test
  public void test_compatible_java_7() {
    new TestSuites();
    new TestSuite();
    assertTrue(true);
  }

  @Test
  public void shouldCollectReportsFromAllTestSuites() {
    final String testFile1 = "one.php";
    final String testFile2 = "two.php";
    final TestSuites testSuites = new TestSuites(new TestSuite(testFile1), new TestSuite(testFile2));
    final List<PhpUnitTestFileReport> reports = testSuites.arrangeSuitesIntoTestFileReports();
    assertThat(reports.size()).isEqualTo(2);
    assertThat(reports).contains(new PhpUnitTestFileReport(testFile1, 0d));
    assertThat(reports).contains(new PhpUnitTestFileReport(testFile2, 0d));
  }

}
