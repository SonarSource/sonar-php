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

import java.io.File;
import org.junit.Test;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.phpunit.JUnitLogParserForPhpUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCaseTest {

  @Test
  public void shouldResolveStatusFromXmlData() throws Exception {
    JUnitLogParserForPhpUnit parser = new JUnitLogParserForPhpUnit();
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "test-cases-status.xml"));
    final TestSuite suite = suites.suites.get(0);
    assertThat(suite.testCases.get(0).getStatus()).isEqualTo(TestCase.Status.OK);
    assertThat(suite.testCases.get(1).getStatus()).isEqualTo(TestCase.Status.ERROR);
    assertThat(suite.testCases.get(2).getStatus()).isEqualTo(TestCase.Status.FAILURE);
    assertThat(suite.testCases.get(3).getStatus()).isEqualTo(TestCase.Status.SKIPPED);
  }

}
