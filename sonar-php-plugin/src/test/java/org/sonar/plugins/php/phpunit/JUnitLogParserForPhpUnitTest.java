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
package org.sonar.plugins.php.phpunit;

import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.phpunit.xml.TestSuites;
import org.sonar.test.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class JUnitLogParserForPhpUnitTest {

  private JUnitLogParserForPhpUnit parser;

  @Before
  public void setUp() throws Exception {
    parser = new JUnitLogParserForPhpUnit();
  }

  @Test
  public void shouldGenerateEmptyTestSuites() {
    final TestSuites suites = parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-with-empty-testsuites.xml"));
    assertThat(suites).isEqualTo(new TestSuites());
  }

  @Test(expected = XStreamException.class)
  public void shouldThrowAnExceptionWhenReportIsInvalid() {
    parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-invalid.xml"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnExceptionWhenReportDoesNotExist() throws Exception {
    parser.parse(new File("target/unexistingFile.xml"));
  }

  @Test
  public void shouldParseComplexNestedSuites() throws Exception {
    final TestSuites suites = parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-junit-report.xml"));
    assertThat(suites.arrangeSuitesIntoTestFileReports().size()).isEqualTo(8);
  }
}
