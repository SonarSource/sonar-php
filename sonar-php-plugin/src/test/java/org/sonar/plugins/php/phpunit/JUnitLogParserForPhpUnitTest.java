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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.phpunit.xml.TestSuites;

import static org.assertj.core.api.Assertions.assertThat;

public class JUnitLogParserForPhpUnitTest {

  private JUnitLogParserForPhpUnit parser;

  @Before
  public void setUp() throws Exception {
    parser = new JUnitLogParserForPhpUnit();
  }

  @Test
  public void shouldGenerateEmptyTestSuites() {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-with-empty-testsuites.xml"));
    assertThat(suites).isEqualTo(new TestSuites(Collections.emptyList()));
  }

  @Test
  public void shouldParseTestSuitesWithoutTime() {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-junit-report-no-time.xml"));
    assertThat(suites).isNotNull();
    List<TestFileReport> reports = suites.arrangeSuitesIntoTestFileReports();
    assertThat(reports).hasSize(2);
    assertThat(reports.get(0).testDurationMilliseconds()).isEqualTo(0);
    assertThat(reports.get(1).testDurationMilliseconds()).isEqualTo(0);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnExceptionWhenReportIsInvalid() {
    parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-invalid.xml"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnExceptionWhenReportDoesNotExist() throws Exception {
    parser.parse(new File("target/unexistingFile.xml"));
  }

  @Test
  public void shouldParseComplexNestedSuites() throws Exception {
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-junit-report.xml"));
    assertThat(suites.arrangeSuitesIntoTestFileReports().size()).isEqualTo(8);
  }
}
