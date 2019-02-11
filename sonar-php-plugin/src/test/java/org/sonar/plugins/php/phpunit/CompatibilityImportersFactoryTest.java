/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.PhpPlugin;

import static org.assertj.core.api.Assertions.assertThat;

public class CompatibilityImportersFactoryTest {

  private static final String BASE_DIR = "/org/sonar/plugins/php/phpunit/sensor/src/";
  private static final SonarRuntime SONARQUBE_6_7 = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.SCANNER);

  private SensorContextTester context;
  private CompatibilityImportersFactory importersFactory;

  @Before
  public void setUp() {
    context = SensorContextTester.create(new File("src/test/resources/"+BASE_DIR));
    importersFactory = new CompatibilityImportersFactory(context);
  }

  @Test
  public void should_create_test_result_and_multi_coverage_importer_starting_from_6_2() throws Exception {
    context.setRuntime(SONARQUBE_6_7);
    context.settings().setProperty(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY, "coverage report");
    assertThat(importersFactory.createCoverageImporter()).isInstanceOf(MultiPathImporter.class);
  }

  @Test
  public void should_fallback_to_legacy_importers_if_only_legacy_properties_are_used() throws Exception {
    context.setRuntime(SONARQUBE_6_7);
    // property simply ignored
    context.settings().setProperty("sonar.php.coverage.reportPath", "coverage report");

    assertThat(importersFactory.createCoverageImporter()).isInstanceOf(MultiPathImporter.class);
  }

  @Test
  public void should_use_importers_appropriate_to_sonarqube_version_when_no_properties_at_all_are_used() throws Exception {
    context.setRuntime(SONARQUBE_6_7);
    assertThat(importersFactory.createCoverageImporter()).isInstanceOf(MultiPathImporter.class);
  }

}
