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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;

import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_TYPE_PHPUNIT;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_TYPE_SUMMARY;

/**
 * Parser selector for Php Depend. Depending on the option set, different type of files will be parsed.
 * As of now choice will be either phpunit-xml or summary-xml
 */
public class PhpDependParserSelector implements BatchExtension {

  private PhpDependPhpUnitReportParser phpunitParser;
  private PhpDependSummaryReportParser summaryParser;
  private PhpDependConfiguration config;
  private static final Logger LOG = LoggerFactory.getLogger(PhpDependParserSelector.class);

  /**
   * @param phpunitParser Parser for Php Depend phpunit-xml report
   * @param summaryParser Parser for Php Depend summary-xml report
   */
  public PhpDependParserSelector(PhpDependPhpUnitReportParser phpunitParser, PhpDependSummaryReportParser summaryParser, PhpDependConfiguration config) {
    super();
    this.phpunitParser = phpunitParser;
    this.summaryParser = summaryParser;
    this.config = config;
  }

  /**
   * Selects appropriate parser depending on configuration option
   *
   * @return PhpDependResultsParser
   * @throws SonarException
   */
  public PhpDependResultsParser select() {
    String reportType = config.getReportType();
    if (PDEPEND_REPORT_TYPE_PHPUNIT.equals(reportType)) {
      LOG.warn("Using deprecated format of PHP Depend report. Support for phpunit-xml report will be removed soon.");
      return phpunitParser;
    } else if (PDEPEND_REPORT_TYPE_SUMMARY.equals(reportType)) {
      return summaryParser;
    } else {
      throw new SonarException("Invalid PHP Depend report type: " + reportType + ". Supported types: phpunit-xml, summary-xml");
    }
  }
}
