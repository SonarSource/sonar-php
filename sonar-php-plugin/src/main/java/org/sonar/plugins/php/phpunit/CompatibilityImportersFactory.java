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
package org.sonar.plugins.php.phpunit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.php.PhpPlugin;

public class CompatibilityImportersFactory {

  public static final String DEPRECATION_WARNING_TEMPLATE = "%s is deprecated as of SonarQube 6.2. Please consider using " + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;
  public static final String SKIPPED_WARNING_TEMPLATE = "Ignoring %s since you are already using " + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY + ". Please remove %<s";
  private static final List<String> LEGACY_PATH_KEYS = Arrays.asList(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY,
    PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY);
  private final SensorContext context;

  public CompatibilityImportersFactory(SensorContext context) {
    this.context = context;
  }

  public ReportImporter createCoverageImporter() {
    if (LEGACY_PATH_KEYS.stream().anyMatch(this::isPropertyUsed) && !multiPathCoverageUsed()) {
      return createLegacyImporters();
    }
    return createMultiCoverageImporter();
  }

  private boolean multiPathCoverageUsed() {
    return context.config().getStringArray(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY).length > 0;
  }

  private static ReportImporter createMultiCoverageImporter() {
    String msg = "coverage";
    String propertyKey = PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;

    CoverageResultImporter singleReportImporter = new CoverageResultImporter(propertyKey, msg);

    return new MultiPathImporter(singleReportImporter, propertyKey, msg);
  }

  private static ReportImporter createLegacyImporters() {
    return new CoverageResultImporter(
      PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY,
      "unit test coverage");
  }

  public List<String> deprecationWarnings() {
    if (multiPathCoverageUsed()) {
      return printWarningForUsedLegacyPaths(SKIPPED_WARNING_TEMPLATE);
    } else {
      return printWarningForUsedLegacyPaths(DEPRECATION_WARNING_TEMPLATE);
    }
  }

  private List<String> printWarningForUsedLegacyPaths(String warningTemplate) {
    return LEGACY_PATH_KEYS.stream().filter(this::isPropertyUsed).map(pathKey -> String.format(warningTemplate, pathKey)).collect(Collectors.toList());
  }

  private boolean isPropertyUsed(String legacyPathKey) {
    return context.config().get(legacyPathKey).isPresent();
  }
}
