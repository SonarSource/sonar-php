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
package org.sonar.plugins.php.phpdepend;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpDependConfigurationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Settings settings;
  private PhpDependConfiguration phpConfig;

  @Before
  public void init() throws Exception {
    settings = Settings.createForComponent(new PhpDependSensor(null, null, null));
    phpConfig = new PhpDependConfiguration(settings, MockUtils.createMockProject().getFileSystem());
  }

  @Test
  public void shouldReturnDefaultValues() {
    assertThat(phpConfig.getCommandLine()).isEqualTo("pdepend");
    assertThat(phpConfig.isSkip()).isFalse();
    assertThat(phpConfig.isAnalyseOnly()).isFalse();
    File report = new File("target/MockProject/target/logs/pdepend.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    assertThat(phpConfig.isWithoutAnnotation()).isFalse();
    assertThat(phpConfig.isBadDocumentation()).isFalse();
    assertThat(phpConfig.getExcludePackages()).isNull();
    assertThat(phpConfig.getArgumentLine()).isNull();
    assertThat(phpConfig.getTimeout()).isEqualTo(DEFAULT_TIMEOUT);
    assertThat(phpConfig.getReportType()).isEqualTo("summary-xml");
    assertThat(phpConfig.getReportFileCommandOption()).isEqualTo("--summary-xml=" + report.getAbsolutePath());
  }

  @Test
  public void shouldReturnCustomProperties() {
    // Given
    settings.setProperty(PhpDependConfiguration.PDEPEND_SKIP_KEY, "true");
    settings.setProperty(PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_KEY, "true");
    settings.setProperty(PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_KEY, "my-pdepend.xml");
    settings.setProperty(PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY, "reports");
    settings.setProperty(PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_KEY, "true");
    settings.setProperty(PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_KEY, "true");
    settings.setProperty(PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY, "a,b,c");
    settings.setProperty(PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY, "--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    settings.setProperty(PhpDependConfiguration.PDEPEND_TIMEOUT_KEY, "120");
    settings.setProperty(PhpDependConfiguration.PDEPEND_REPORT_TYPE, "summary-xml");

    // Verify
    assertThat(phpConfig.getCommandLine()).isEqualTo("pdepend");
    assertThat(phpConfig.isSkip()).isTrue();
    assertThat(phpConfig.isAnalyseOnly()).isTrue();
    File report = new File("target/MockProject/target/reports/my-pdepend.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    assertThat(phpConfig.isWithoutAnnotation()).isTrue();
    assertThat(phpConfig.isBadDocumentation()).isTrue();
    assertThat(phpConfig.getExcludePackages()).isEqualTo("a,b,c");
    assertThat(phpConfig.getArgumentLine()).isEqualTo("--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    assertThat(phpConfig.getTimeout()).isEqualTo(120);
    assertThat(phpConfig.getReportType()).isEqualTo("summary-xml");
    assertThat(phpConfig.getReportFileCommandOption()).isEqualTo("--summary-xml=" + report.getAbsolutePath());
  }

  @Test
  public void shouldFailIfBadReportType() {
    settings.setProperty(PhpDependConfiguration.PDEPEND_REPORT_TYPE, "foo-xml");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid PHP Depend report type: foo-xml. Supported types: phpunit-xml, summary-xml");

    phpConfig.getReportFileCommandOption();
  }

  @Test
  public void shouldReturnSuffixesCommandOption() throws Exception {
    assertThat(phpConfig.getSuffixesCommandOption(new Php(settings))).isEqualTo("--suffix=php,php3,php4,php5,phtml,inc");
  }

}
