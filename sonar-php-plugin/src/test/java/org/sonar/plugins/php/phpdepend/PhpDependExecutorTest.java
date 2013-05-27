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
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_KEY;

public class PhpDependExecutorTest {

  private Settings settings;
  private PhpDependExecutor executor;

  @Before
  public void init() throws Exception {
    settings = Settings.createForComponent(new PhpDependSensor(null, null, null));
    Project project = MockUtils.createMockProject();
    PhpDependConfiguration configuration = new PhpDependConfiguration(settings, project.getFileSystem());
    executor = new PhpDependExecutor(new Php(), configuration);
  }

  @Test
  public void testGetExecutedTool() throws Exception {
    assertThat(executor.getExecutedTool(), is("PHP Depend"));
  }

  @Test
  public void testSimpleCommandLine() {
    List<String> commandLine = executor.getCommandLineArguments();
    assertThat(commandLine.get(0)).isEqualTo("--summary-xml=" + new File("target/MockProject/target/sonar/pdepend.xml").getAbsolutePath());
    assertThat(commandLine.get(1)).isEqualTo("--suffix=php,php3,php4,php5,phtml,inc");
    assertThat(commandLine.get(2)).isEqualTo(new File("target/MockProject/src").getAbsolutePath());
  }

  @Test
  public void testCommandLineWithSeveralParameters() {
    // Given
    settings.setProperty(PDEPEND_EXCLUDE_PACKAGE_KEY, "foo,bar");
    settings.setProperty(PDEPEND_WITHOUT_ANNOTATION_KEY, "true");
    settings.setProperty(PDEPEND_BAD_DOCUMENTATION_KEY, "true");
    settings.setProperty(PDEPEND_ARGUMENT_LINE_KEY, "  --foo=bar --foo2=bar2 ");

    // Verify
    List<String> commandLine = executor.getCommandLineArguments();
    assertThat(commandLine.get(0)).isEqualTo("--summary-xml=" + new File("target/MockProject/target/sonar/pdepend.xml").getAbsolutePath());
    assertThat(commandLine.get(1)).isEqualTo("--suffix=php,php3,php4,php5,phtml,inc");
    assertThat(commandLine.get(2)).isEqualTo("--exclude=foo,bar");
    assertThat(commandLine.get(3)).isEqualTo("--bad-documentation");
    assertThat(commandLine.get(4)).isEqualTo("--without-annotations");
    assertThat(commandLine.get(5)).isEqualTo("--foo=bar");
    assertThat(commandLine.get(6)).isEqualTo("--foo2=bar2");
    assertThat(commandLine.get(7)).isEqualTo(new File("target/MockProject/src").getAbsolutePath());
  }

  /**
   * SONARPLUGINS-1718
   */
  @Test
  public void testCommandLineWithStarInArgumentLineProperty() {
    // Given
    settings.setProperty(PDEPEND_ARGUMENT_LINE_KEY, "--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");

    // Verify
    assertThat(executor.getCommandLineArguments().get(2)).isEqualTo("--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
  }

  @Test
  public void testTestCommand() throws Exception {
    List<String> commandLine = executor.getTestCommandLine();
    assertThat(commandLine.size()).isEqualTo(2);
    assertThat(commandLine.get(1)).isEqualTo("--version");
  }

  @Test
  public void testPHAREmbeddedURL() throws Exception {
    assertThat(executor.getPHAREmbeddedURL()).isNotNull();
  }
}
