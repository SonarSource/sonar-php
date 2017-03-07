/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.PhpPlugin;

public class CompatibilityImportersFactory {

  public static final String DEPRECATION_WARNING_TEMPLATE = "%s is deprecated as of SonarQube 6.2. Please consider using " + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;
  public static final String SKIPPED_WARNING_TEMPLATE = "Ignoring %s since you are already using " + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY + ". Please remove %<s";
  public static final String NOT_YET_SUPPORTED_WARNING = PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY + " is only supported by SonarQube 6.2 and above";
  private static final List<String> LEGACY_PATH_KEYS = Arrays.asList(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY,
    PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY);
  private final SensorContext context;

  public CompatibilityImportersFactory(SensorContext context) {
    this.context = context;
  }

  public List<ReportImporter> createCoverageImporters() {
    if (!supportsMultiPathCoverage()) {
      return createLegacyImporters();
    }
    if (LEGACY_PATH_KEYS.stream().anyMatch(this::isPropertyUsed) && !multiPathCoverageUsed()) {
      return createLegacyImporters();
    }
    return createMultiCoverageImporter();
  }

  private boolean supportsMultiPathCoverage() {
    return context.getSonarQubeVersion().isGreaterThanOrEqual(Version.create(6, 2));
  }

  private boolean multiPathCoverageUsed() {
    return context.settings().getStringArray(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY).length > 0;
  }

  private static List<ReportImporter> createMultiCoverageImporter() {
    String msg = "coverage";
    String propertyKey = PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY;

    CoverageResultImporter singleReportImporter = new CoverageResultImporter(propertyKey,
      msg,
      CoreMetrics.LINES_TO_COVER,
      CoreMetrics.UNCOVERED_LINES,
      CoverageType.UNIT);

    return Collections.singletonList(new MultiPathImporter(singleReportImporter, propertyKey, msg));
  }

  private static List<ReportImporter> createLegacyImporters() {
    List<ReportImporter> importers = new ArrayList<>();

    importers.add(new CoverageResultImporter(
      PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY,
      "unit test coverage",
      CoreMetrics.LINES_TO_COVER,
      CoreMetrics.UNCOVERED_LINES,
      CoverageType.UNIT));

    importers.add(new CoverageResultImporter(
      PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY,
      "integration test coverage",
      CoreMetrics.IT_LINES_TO_COVER,
      CoreMetrics.IT_UNCOVERED_LINES,
      CoverageType.IT));

    importers.add(new CoverageResultImporter(
      PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY,
      "overall coverage",
      CoreMetrics.OVERALL_LINES_TO_COVER,
      CoreMetrics.OVERALL_UNCOVERED_LINES,
      CoverageType.OVERALL));

    return importers;
  }

  public List<String> deprecationWarnings() {
    if (supportsMultiPathCoverage()) {
      if (multiPathCoverageUsed()) {
        return printWarningForUsedLegacyPaths(SKIPPED_WARNING_TEMPLATE);
      } else {
        return printWarningForUsedLegacyPaths(DEPRECATION_WARNING_TEMPLATE);
      }

    } else if (multiPathCoverageUsed()) {
      return Collections.singletonList(NOT_YET_SUPPORTED_WARNING);
    }

    return new ArrayList<>();
  }

  private List<String> printWarningForUsedLegacyPaths(String warningTemplate) {
    return LEGACY_PATH_KEYS.stream().filter(this::isPropertyUsed).map(pathKey -> String.format(warningTemplate, pathKey)).collect(Collectors.toList());
  }

  private boolean isPropertyUsed(String legacyPathKey) {
    return context.settings().getString(legacyPathKey) != null;
  }
}
