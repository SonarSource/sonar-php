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

import org.apache.commons.configuration.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.MockUtils;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_TYPE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_TYPE_DEFVALUE;
import static org.junit.Assert.assertThat;

/**
 * The Class PhpDependParserSelectorTest.
 */
public class PhpDependParserSelectorTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSelectThrowsExceptionWhenInvalidReportUsed() {
    String reportType = "invalid-xml";
    Configuration c = mock(Configuration.class);
    when(c.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn(reportType);
    Project project = MockUtils.createMockProject(c);

    PhpDependPhpUnitReportParser phpunitParser = mock(PhpDependPhpUnitReportParser.class);
    PhpDependSummaryReportParser summaryParser = mock(PhpDependSummaryReportParser.class);
    PhpDependParserSelector parserSelector = new PhpDependParserSelector(phpunitParser, summaryParser);
    PhpDependConfiguration conf = new PhpDependConfiguration(project);

    exception.expect(SonarException.class);
    exception.expectMessage("Invalid PHP Depend report type: " + reportType + ". Supported types: phpunit-xml, summary-xml");
    parserSelector.select(conf);
  }

  @Test
  public void testParserSelectorChoosesPhpUnitParser() {
    String reportType = "phpunit-xml";
    Configuration c = mock(Configuration.class);
    when(c.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn(PDEPEND_REPORT_TYPE_DEFVALUE);
    Project project = MockUtils.createMockProject(c);

    PhpDependPhpUnitReportParser phpunitParser = mock(PhpDependPhpUnitReportParser.class);
    PhpDependSummaryReportParser summaryParser = mock(PhpDependSummaryReportParser.class);
    PhpDependParserSelector parserSelector = new PhpDependParserSelector(phpunitParser, summaryParser);
    PhpDependConfiguration conf = new PhpDependConfiguration(project);

    PhpDependResultsParser parser = parserSelector.select(conf);
    assertThat(parser, is(PhpDependPhpUnitReportParser.class));
  }

  @Test
  public void testParserSelectorChoosesSummaryParser() {
    String reportType = "summary-xml";
    Configuration c = mock(Configuration.class);
    when(c.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn(reportType);
    Project project = MockUtils.createMockProject(c);

    PhpDependPhpUnitReportParser phpunitParser = mock(PhpDependPhpUnitReportParser.class);
    PhpDependSummaryReportParser summaryParser = mock(PhpDependSummaryReportParser.class);
    PhpDependParserSelector parserSelector = new PhpDependParserSelector(phpunitParser, summaryParser);
    PhpDependConfiguration conf = new PhpDependConfiguration(project);

    PhpDependResultsParser parser = parserSelector.select(conf);
    assertThat(parser, is(PhpDependSummaryReportParser.class));
  }
}
