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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY;

public class PhpmdExecutorTest {

  @Mock
  private PhpmdProfileExporter exporter;

  @Mock
  private RulesProfile profile;

  private Settings settings;
  private PhpmdExecutor executor;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    settings = Settings.createForComponent(new PhpmdSensor(null, null, null, null));
    Project project = MockUtils.createMockProject();
    PhpmdConfiguration configuration = new PhpmdConfiguration(settings, project.getFileSystem());
    executor = new PhpmdExecutor(new Php(), configuration, exporter, profile);
  }

  @Test
  public void testGetExecutedTool() {
    assertThat(executor.getExecutedTool(), is("PHPMD"));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()} .
   */
  @Test
  public void testSimpleCommandLine() {
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.get(0)).startsWith("phpmd");
    assertThat(commandLine.get(1)).isEqualTo(new File("target/MockProject/src").getAbsolutePath());
    assertThat(commandLine.get(2)).isEqualTo("xml");
    assertThat(commandLine.get(3)).isEqualTo("codesize,unusedcode,naming");
    assertThat(commandLine.get(4)).isEqualTo("--reportfile");
    assertThat(commandLine.get(5)).isEqualTo(new File("target/MockProject/target/logs/pmd.xml").getAbsolutePath());
    assertThat(commandLine.get(6)).isEqualTo("--suffixes");
    assertThat(commandLine.get(7)).isEqualTo("php,php3,php4,php5,phtml,inc");
  }

  @Test
  public void testCommandLineWithSeveralParameters() {
    // Given
    settings.setProperty(PHPMD_ARGUMENT_LINE_KEY, "  --foo=bar --foo2=bar2 ");

    // Verify
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.get(8)).isEqualTo("--foo=bar");
    assertThat(commandLine.get(9)).isEqualTo("--foo2=bar2");
  }

}
