/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

/**
 * 
 */
package org.sonar.plugins.php.core;

/**
 * @author Akram Ben Aissi
 * 
 */
public class PhpPluginConfiguration {

  public static final String PHP_FILE_SUFFIXES_MESSAGE = "File suffixes";
  public static final String PHP_FILE_SUFFIXES_DESCRIPTION = "Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.";

  public static final String PHPCS_EXECUTE_MESSAGE = "Execute PHP_CodeSniffer";
  public static final String PHPCS_EXECUTE_DESCRIPTION = "If true PhpCodeSniffer engine will be run and its violations will be present in Sonar dashboard.";

  public static final String PDEPEND_EXECUTE_MESSAGE = "Execute Pdepend";
  public static final String PDEPEND_EXECUTE_DESCRIPTION = "If true PDepend engine will be run and its violations will be present in Sonar dashboard.";

  public static final String PHPUNIT_EXECUTE_MESSAGE = "Execute PHPUnit";
  public static final String PHPUNIT_EXECUTE_DESCRIPTION = "If true PHPUnit tests will be run and unit tests counts will be present in Sonar dashboard.";

  public static final String PHPUNIT_COVERAGE_EXECUTE_MESSAGE = "Execute PHPUnit coverage";
  public static final String PHPUNIT_COVERAGE_EXECUTE_DESCRIPTION = "If true code coverage measures will be computed.";

  public static final String PHPCPD_EXECUTE_MESSAGE = "Execute PhpCpd";
  public static final String PHPCPD_EXECUTE_DESCRIPTION = "If true copy/paste measures for PHP will be computed.";

  public static final String PHPCPD_MIN_LINES_MESSAGE = "Minimum number of identical lines";
  public static final String PHPCPD_MIN_LINES_DESCRIPTION = "The minimum number of identical lines to consider to detect a copy/paste.";

  public static final String PHPCPD_MIN_TOKENS_MESSAGE = "Minimum number of identical tokens";
  public static final String PHPCPD_MIN_TOKENS_DESCRIPTION = "The minimum number of identical tokens to consider to detect a copy/paster";

  /**
   * hidden constructor
   */
  private PhpPluginConfiguration() {
  }

}
