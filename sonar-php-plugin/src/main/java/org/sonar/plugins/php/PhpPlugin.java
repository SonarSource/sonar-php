/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.NoSonarAndCommentedOutLocSensor;
import org.sonar.plugins.php.core.PhpCommonRulesDecorator;
import org.sonar.plugins.php.core.PhpCommonRulesEngine;
import org.sonar.plugins.php.duplications.PhpCPDMapping;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitItCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitOverallCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitSensor;

import java.util.List;

public class PhpPlugin extends SonarPlugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";
  public static final String PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.overallReportPath";
  public static final String PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.itReportPath";
  public static final String PHPUNIT_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.reportPath";
  public static final String PHPUNIT_TESTS_REPORT_PATH_KEY = "sonar.php.tests.reportPath";

  public static final String PHP_CATEGORY = "PHP";
  public static final String GENERAL_SUBCATEGORY = "General";
  public static final String PHPUNIT_SUBCATEGORY = "PHPUnit";

  /**
   * Gets the extensions.
   *
   * @return the extensions
   * @see org.sonar.api.SonarPlugin#getExtensions()
   */
  @Override
  public List getExtensions() {
    return ImmutableList.of(

      Php.class,

      // Core extensions
      NoSonarAndCommentedOutLocSensor.class,

      // Duplications
      PhpCPDMapping.class,

      // Common rules
      PhpCommonRulesEngine.class,
      PhpCommonRulesDecorator.class,

      PHPSensor.class,

      PHPRulesDefinition.class,
      PHPProfile.class,
      PSR2Profile.class,
      DrupalProfile.class,

      // PhpUnit
      PhpUnitSensor.class,
      PhpUnitResultParser.class,
      PhpUnitCoverageResultParser.class,
      PhpUnitItCoverageResultParser.class,
      PhpUnitOverallCoverageResultParser.class,

      // Properties
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(Php.DEFAULT_FILE_SUFFIXES)
        .name("File Suffixes")
        .description("Comma-separated list of suffixes of PHP files to analyze.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(PHPUNIT_TESTS_REPORT_PATH_KEY)
        .name("Unit Test Report")
        .description("Path to the PHPUnit unit test execution report file. The path may be either absolute or relative to the project base directory.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(PHPUNIT_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(PHPUNIT_COVERAGE_REPORT_PATH_KEY)
        .name("Coverage Report")
        .description("Path to the PHPUnit code coverage report file. The path may be either absolute or relative to the project base directory.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(PHPUNIT_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY)
        .name("IT Coverage Report")
        .description("Path to the PHPUnit integration test code coverage report file. The path may be either absolute or relative to the project base directory.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(PHPUNIT_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY)
        .name("Overall Coverage Report")
        .description("Path to the PHPUnit overall code coverage report file. The path may be either absolute or relative to the project base directory.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(PHPUNIT_SUBCATEGORY)
        .build()
    );
  }
}
