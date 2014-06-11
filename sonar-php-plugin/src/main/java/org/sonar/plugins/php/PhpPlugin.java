/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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
package org.sonar.plugins.php;

import com.google.common.collect.ImmutableList;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.NoSonarAndCommentedOutLocSensor;
import org.sonar.plugins.php.core.PhpCommonRulesEngine;
import org.sonar.plugins.php.core.PhpSourceCodeColorizer;
import org.sonar.plugins.php.core.PhpSourceImporter;
import org.sonar.plugins.php.duplications.PhpCPDMapping;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitSensor;

import java.util.List;

@Properties({
  @Property(
    key = PhpPlugin.FILE_SUFFIXES_KEY,
    defaultValue = Php.DEFAULT_FILE_SUFFIXES,
    name = "File Suffixes",
    description = "Comma-separated list of suffixes of PHP files to analyze.",
    global = true,
    project = true),
  @Property(key = PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY,
    name = "Unit Test Report",
    project = true,
    global = true,
    description = "Path to the PHPUnit unit test execution report file. The path may be either absolute or relative to the project base directory."),
  @Property(key = PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY,
    name = "Coverage Report",
    project = true,
    global = true,
    description = "Path to the PHPUnit code coverage report file. The path may be either absolute or relative to the project base directory.")
})
public class PhpPlugin extends SonarPlugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";
  public static final String PHPUNIT_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.reportPath";
  public static final String PHPUNIT_TESTS_REPORT_PATH_KEY = "sonar.php.tests.reportPath";

  /**
   * Gets the extensions.
   *
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List getExtensions() {
    return ImmutableList.of(

      Php.class,

      // Core extensions
      PhpSourceImporter.class,
      PhpSourceCodeColorizer.class,
      NoSonarAndCommentedOutLocSensor.class,

      // Duplications
      PhpCPDMapping.class,

      // Common rules
      PhpCommonRulesEngine.class,

      PHPSquidSensor.class,

      PHPRuleRepository.class,
      PHPProfile.class,

      // PhpUnit
      PhpUnitSensor.class,
      PhpUnitResultParser.class,
      PhpUnitCoverageResultParser.class);
  }
}
