/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php;

import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.reports.phpstan.PhpStanRulesDefinition;
import org.sonar.plugins.php.reports.phpstan.PhpStanSensor;
import org.sonar.plugins.php.reports.phpunit.PhpUnitSensor;
import org.sonar.plugins.php.reports.psalm.PsalmRulesDefinition;
import org.sonar.plugins.php.reports.psalm.PsalmSensor;
import org.sonar.plugins.php.warning.DefaultAnalysisWarningsWrapper;

import static org.sonar.plugins.php.reports.phpstan.PhpStanSensor.PHPSTAN_REPORT_PATH_KEY;
import static org.sonar.plugins.php.reports.phpunit.PhpUnitSensor.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;
import static org.sonar.plugins.php.reports.phpunit.PhpUnitSensor.PHPUNIT_TESTS_REPORT_PATH_KEY;
import static org.sonar.plugins.php.reports.psalm.PsalmSensor.PSALM_REPORT_PATH_KEY;

public class PhpPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";
  public static final String PHP_EXCLUSIONS_KEY = "sonar.php.exclusions";
  public static final String PHP_EXCLUSIONS_DEFAULT_VALUE = "**/vendor/**";
  public static final String PHP_FRAMEWORK_DETECTION = "sonar.php.frameworkDetection";
  public static final String PHP_FRAMEWORK_DETECTION_DEFAULT_VALUE = "true";

  public static final String PHP_CATEGORY = "PHP";
  public static final String GENERAL_SUBCATEGORY = "General";
  public static final String PHPUNIT_SUBCATEGORY = "PHPUnit";
  private static final String EXTERNAL_ANALYZERS_SUBCATEGORY = "External Analyzers";

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
      PhpExclusionsFileFilter.class,

      // Properties
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(Php.DEFAULT_FILE_SUFFIXES)
        .name("File Suffixes")
        .description("List of suffixes of PHP files to analyze.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(PHP_EXCLUSIONS_KEY)
        .defaultValue(PHP_EXCLUSIONS_DEFAULT_VALUE)
        .name("PHP Exclusions")
        .description("List of file path patterns to be excluded from analysis of PHP files.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .multiValues(true)
        .build(),

      PropertyDefinition.builder(PHP_FRAMEWORK_DETECTION)
        .defaultValue(PHP_FRAMEWORK_DETECTION_DEFAULT_VALUE)
        .name("PHP Framework detection")
        .description("Enable the detection of PHP framework in analyzed file, which adapt some rules behavior.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());

    if (context.getRuntime().getProduct() != SonarProduct.SONARLINT) {
      addPhpUnitExtensions(context);
      addPhpStanExtensions(context);
      addPsalmExtensions(context);
    }

    context.addExtension(DefaultAnalysisWarningsWrapper.class);
  }

  private static void addPhpUnitExtensions(Context context) {
    context.addExtensions(PhpUnitSensor.class,
      PropertyDefinition.builder(PHPUNIT_TESTS_REPORT_PATH_KEY)
        .name("Unit Test Report")
        .description("Comma-separated list of paths to PHPUnit unit test execution report files. Paths may be either absolute or relative to the project base directory.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .multiValues(true)
        .subCategory(PHPUNIT_SUBCATEGORY)
        .build(),
      PropertyDefinition.builder(PHPUNIT_COVERAGE_REPORT_PATHS_KEY)
        .name("Coverage Reports")
        .description("List of PHPUnit code coverage report files. Each path can be either absolute or relative.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(PHP_CATEGORY)
        .multiValues(true)
        .subCategory(PHPUNIT_SUBCATEGORY)
        .build());
  }

  private static void addPhpStanExtensions(Context context) {
    context.addExtensions(
      PhpStanRulesDefinition.class,
      PhpStanSensor.class,
      PropertyDefinition.builder(PHPSTAN_REPORT_PATH_KEY)
        .name("PHPStan Report Files")
        .description("Paths (absolute or relative) to report files with PHPStan issues.")
        .category(EXTERNAL_ANALYZERS_SUBCATEGORY)
        .subCategory(PHP_CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build());
  }

  private static void addPsalmExtensions(Context context) {
    context.addExtensions(
      PsalmRulesDefinition.class,
      PsalmSensor.class,
      PropertyDefinition.builder(PSALM_REPORT_PATH_KEY)
        .name("Psalm Report Files")
        .description("Paths (absolute or relative) to report files with Psalm issues.")
        .category(EXTERNAL_ANALYZERS_SUBCATEGORY)
        .subCategory(PHP_CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build());
  }

}
