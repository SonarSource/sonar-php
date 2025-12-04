/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.reports.phpunit.JUnitLogParserForPhpUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TestCaseTest {

  @Test
  void shouldResolveStatusFromXmlData() throws Exception {
    JUnitLogParserForPhpUnit parser = new JUnitLogParserForPhpUnit();
    final TestSuites suites = parser.parse(new File("src/test/resources/" + PhpTestUtils.PHPUNIT_REPORT_DIR + "test-cases-status.xml"));
    final TestSuite suite = suites.suites.get(0);
    assertThat(suite.testCases.get(0).getStatus()).isEqualTo(TestCase.Status.OK);
    assertThat(suite.testCases.get(1).getStatus()).isEqualTo(TestCase.Status.ERROR);
    assertThat(suite.testCases.get(2).getStatus()).isEqualTo(TestCase.Status.FAILURE);
    assertThat(suite.testCases.get(3).getStatus()).isEqualTo(TestCase.Status.SKIPPED);
  }

}
