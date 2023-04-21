/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.reports.phpunit;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

public class PhpUnitSensor implements Sensor {

  public static final String PHPUNIT_COVERAGE_REPORT_PATHS_KEY = "sonar.php.coverage.reportPaths";
  public static final String PHPUNIT_TESTS_REPORT_PATH_KEY = "sonar.php.tests.reportPath";
  private final TestResultImporter testResultImporter;
  private final CoverageResultImporter coverageResultImporter;

  public PhpUnitSensor(AnalysisWarningsWrapper analysisWarningsWrapper) {
    this.testResultImporter = new TestResultImporter(analysisWarningsWrapper);
    this.coverageResultImporter = new CoverageResultImporter(analysisWarningsWrapper);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(Php.KEY)
      .name("PHPUnit report sensor");
  }

  @Override
  public void execute(SensorContext context) {
    testResultImporter.execute(context);
    coverageResultImporter.execute(context);
  }
}
