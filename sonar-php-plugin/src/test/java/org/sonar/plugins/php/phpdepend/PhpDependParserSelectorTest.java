/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php.phpdepend;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.utils.SonarException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The Class PhpDependParserSelectorTest.
 */
public class PhpDependParserSelectorTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private PhpDependConfiguration phpConfig;

  @Mock
  private PhpDependPhpUnitReportParser phpunitParser;

  @Mock
  private PhpDependSummaryReportParser summaryParser;

  private PhpDependParserSelector parserSelector;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    parserSelector = new PhpDependParserSelector(phpunitParser, summaryParser, phpConfig);
  }

  @Test
  public void testSelectThrowsExceptionWhenInvalidReportUsed() {
    when(phpConfig.getReportType()).thenReturn("invalid-xml");

    exception.expect(SonarException.class);
    exception.expectMessage("Invalid PHP Depend report type: invalid-xml. Supported types: phpunit-xml, summary-xml");

    parserSelector.select();
  }

  @Test
  public void testParserSelectorChoosesPhpUnitParser() {
    when(phpConfig.getReportType()).thenReturn(PhpDependConfiguration.PDEPEND_REPORT_TYPE_PHPUNIT);

    assertThat(parserSelector.select()).isEqualTo(phpunitParser);
  }

  @Test
  public void testParserSelectorChoosesSummaryParser() {
    when(phpConfig.getReportType()).thenReturn(PhpDependConfiguration.PDEPEND_REPORT_TYPE_SUMMARY);

    assertThat(parserSelector.select()).isEqualTo(summaryParser);
  }
}
