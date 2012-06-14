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
package org.sonar.plugins.php.codesniffer;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.php.MockUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpCodesnifferConfigurationTest {

  private Settings settings;
  private PhpCodeSnifferConfiguration phpConfig;

  @Before
  public void init() throws Exception {
    settings = Settings.createForComponent(new PhpCodeSnifferSensor(null, null, null, null));
    phpConfig = createPhpCodesnifferConfiguration(settings);
  }

  @Test
  public void shouldReturnDefaultValues() {
    assertThat(phpConfig.getCommandLine()).isEqualTo("phpcs");
    assertThat(phpConfig.isSkip()).isFalse();
    assertThat(phpConfig.isAnalyseOnly()).isFalse();
    File report = new File("target/MockProject/target/logs/codesniffer.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    assertThat(phpConfig.getStandard()).isNull();
    assertThat(phpConfig.getLevel()).isNull();
    assertThat(phpConfig.getSeverityModifier()).isNull();
    assertThat(phpConfig.getArgumentLine()).isNull();
    assertThat(phpConfig.getTimeout()).isEqualTo(DEFAULT_TIMEOUT);
  }

  @Test
  public void shouldReturnCustomProperties() {
    // Given
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_SKIP_KEY, "true");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_KEY, "true");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY, "codesniffer-summary.xml");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY, "reports");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_STANDARD_ARGUMENT_KEY, "PEAR");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY, "--level=");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_SEVERITY_KEY, "error");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_ARGUMENT_LINE_KEY, "--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    settings.setProperty(PhpCodeSnifferConfiguration.PHPCS_TIMEOUT_KEY, "120");

    // Verify
    assertThat(phpConfig.getCommandLine()).isEqualTo("phpcs");
    assertThat(phpConfig.isSkip()).isTrue();
    assertThat(phpConfig.isAnalyseOnly()).isTrue();
    File report = new File("target/MockProject/target/reports/codesniffer-summary.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    assertThat(phpConfig.getStandard()).isEqualTo("PEAR");
    assertThat(phpConfig.getSeverityModifier()).isEqualTo("--level=");
    assertThat(phpConfig.getLevel()).isEqualTo("error");
    assertThat(phpConfig.getArgumentLine()).isEqualTo("--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    assertThat(phpConfig.getTimeout()).isEqualTo(120);
  }

  private PhpCodeSnifferConfiguration createPhpCodesnifferConfiguration(Settings settings) {
    Project project = MockUtils.createMockProject();

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter exporter = mock(PhpCodeSnifferProfileExporter.class);
    RuleFinder finder = mock(RuleFinder.class);

    PhpCodeSnifferConfiguration phpConfig = new PhpCodeSnifferConfiguration(settings, project, exporter, profile, finder);
    return phpConfig;
  }
}
