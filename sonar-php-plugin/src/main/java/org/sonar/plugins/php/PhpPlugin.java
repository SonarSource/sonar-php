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
package org.sonar.plugins.php;

import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.php.api.Php;

public class PhpPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";
  public static final String PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.overallReportPath";
  public static final String PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.itReportPath";
  public static final String PHPUNIT_COVERAGE_REPORT_PATH_KEY = "sonar.php.coverage.reportPath";
  public static final String PHPUNIT_COVERAGE_REPORT_PATHS_KEY = "sonar.php.coverage.reportPaths";
  public static final String PHPUNIT_TESTS_REPORT_PATH_KEY = "sonar.php.tests.reportPath";

  public static final String PHP_CATEGORY = "PHP";
  public static final String GENERAL_SUBCATEGORY = "General";
  public static final String PHPUNIT_SUBCATEGORY = "PHPUnit";

  private static final String DEPRECATION_MESSAGE = "DEPRECATED: use " + PHPUNIT_COVERAGE_REPORT_PATHS_KEY + ". ";
  private static final String REPORT_PATH_DESCRIPTION_TEMPLATE = "%sPath to the PHPUnit %s report file. The path may be either absolute or relative to the project base directory.";

  @Override
  public void define(Context context) {
    context.addExtensions(

      // Language
      Php.class,

      // Sensor
      PHPSensor.class,
      PhpIniSensor.class,

      // Rules and profiles
      PHPRulesDefinition.class,
      PHPProfileDefinition.class,
      PSR2ProfileDefinition.class,
      DrupalProfileDefinition.class,

      // Properties
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(Php.DEFAULT_FILE_SUFFIXES)
        .name("File Suffixes")
        .description("List of suffixes of PHP files to analyze.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());

    if (context.getRuntime().getProduct() != SonarProduct.SONARLINT) {
      context.addExtensions(
        PropertyDefinition.builder(PHPUNIT_TESTS_REPORT_PATH_KEY)
          .name("Unit Test Report")
          .description(String.format(REPORT_PATH_DESCRIPTION_TEMPLATE, "", "unit test execution"))
          .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
          .category(PHP_CATEGORY)
          .subCategory(PHPUNIT_SUBCATEGORY)
          .build(),

        PropertyDefinition.builder(PHPUNIT_COVERAGE_REPORT_PATH_KEY)
          .name("Coverage Report")
          .description(String.format(REPORT_PATH_DESCRIPTION_TEMPLATE, DEPRECATION_MESSAGE, "code coverage"))
          .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
          .category(PHP_CATEGORY)
          .subCategory(PHPUNIT_SUBCATEGORY)
          .build(),

        PropertyDefinition.builder(PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY)
          .name("IT Coverage Report")
          .description(String.format(REPORT_PATH_DESCRIPTION_TEMPLATE, DEPRECATION_MESSAGE, "integration test code coverage"))
          .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
          .category(PHP_CATEGORY)
          .subCategory(PHPUNIT_SUBCATEGORY)
          .build(),

        PropertyDefinition.builder(PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY)
          .name("Overall Coverage Report")
          .description(String.format(REPORT_PATH_DESCRIPTION_TEMPLATE, DEPRECATION_MESSAGE, "overall code coverage"))
          .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
          .category(PHP_CATEGORY)
          .subCategory(PHPUNIT_SUBCATEGORY)
          .build());
    }
    context.addExtension(PropertyDefinition.builder(PHPUNIT_COVERAGE_REPORT_PATHS_KEY)
      .name("Coverage Reports")
      .description("List of PHPUnit code coverage report files. Each path can be either absolute or relative.")
      .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
      .category(PHP_CATEGORY)
      .multiValues(true)
      .subCategory(PHPUNIT_SUBCATEGORY)
      .build());

  }

}
