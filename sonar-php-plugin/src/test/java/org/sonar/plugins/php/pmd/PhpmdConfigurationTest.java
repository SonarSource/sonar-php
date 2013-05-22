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
package org.sonar.plugins.php.pmd;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.plugins.php.MockUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;

/**
 * The Class PhpmdConfigurationTest.
 */
public class PhpmdConfigurationTest {

  private Settings settings;
  private PhpmdConfiguration phpConfig;

  @Before
  public void init() throws Exception {
    settings = Settings.createForComponent(new PhpmdSensor(null, null, null, null));
    phpConfig = new PhpmdConfiguration(settings, MockUtils.createMockProject().getFileSystem());
  }

  @Test
  public void shouldReturnDefaultValues() {
    assertThat(phpConfig.getCommandLine()).isEqualTo("phpmd");
    assertThat(phpConfig.isSkip()).isFalse();
    assertThat(phpConfig.isAnalyseOnly()).isFalse();
    File report = new File("target/MockProject/target/logs/pmd.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    assertThat(phpConfig.getLevel()).isEqualTo("2");
    assertThat(phpConfig.getArgumentLine()).isNull();
    assertThat(phpConfig.getTimeout()).isEqualTo(DEFAULT_TIMEOUT);
  }

  @Test
  public void shouldReturnCustomProperties() {
    // Given
    settings.setProperty(PhpmdConfiguration.PHPMD_SKIP_KEY, "true");
    settings.setProperty(PhpmdConfiguration.PHPMD_ANALYZE_ONLY_KEY, "true");
    settings.setProperty(PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_KEY, "my-pmd.xml");
    settings.setProperty(PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_KEY, "reports");
    settings.setProperty(PhpmdConfiguration.PHPMD_LEVEL_ARGUMENT_KEY, "5");
    settings.setProperty(PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY, "--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    settings.setProperty(PhpmdConfiguration.PHPMD_TIMEOUT_KEY, "120");

    // Verify
    assertThat(phpConfig.getCommandLine()).isEqualTo("phpmd");
    assertThat(phpConfig.isSkip()).isTrue();
    assertThat(phpConfig.isAnalyseOnly()).isTrue();
    File report = new File("target/MockProject/target/reports/my-pmd.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    assertThat(phpConfig.getLevel()).isEqualTo("5");
    assertThat(phpConfig.getArgumentLine()).isEqualTo("--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    assertThat(phpConfig.getTimeout()).isEqualTo(120);
  }

  @Test
  public void shouldReturnDefaultRuleSets() {
    assertThat(phpConfig.getRulesets()).isEqualTo("codesize,unusedcode,naming");
  }
}
