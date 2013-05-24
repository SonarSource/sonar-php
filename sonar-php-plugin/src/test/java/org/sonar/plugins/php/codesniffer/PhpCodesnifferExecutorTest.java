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

import com.google.common.collect.Lists;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.PhpConstants;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SEVERITY_OR_LEVEL_MODIFIER;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY;

public class PhpCodesnifferExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLineArguments()}.
   */
  @Test
  public void testGetCommandLine() {
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);

    PhpCodeSnifferExecutor executor = createExecutor(new Settings(), c);
    executor.getCommandLineArguments();
    assertThat(executor.getExecutedTool(), is("PHPCodeSniffer"));
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLineArguments()}.
   */
  @Test
  public void testGetCommandLineWithExtensions() {
    Settings settings = new Settings();
    settings.setProperty(PhpConstants.FILE_SUFFIXES_KEY, "php, php2");
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);

    PhpCodeSnifferExecutor executor = createExecutor(settings, c);
    List<String> commandLine = executor.getCommandLineArguments();

    String expected = "--extensions=php,php2";
    assertThat(commandLine).contains(expected);
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLineArguments()}.
   */
  @Test
  public void testGetCommandLineWithSeverityLevel() {
    Settings settings = new Settings();
    settings.setProperty(PhpConstants.FILE_SUFFIXES_KEY, "php, php2");
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);

    Configuration configuration = mock(Configuration.class);
    String modifierKey = PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY;
    when(configuration.getString(modifierKey, PHPCS_SEVERITY_OR_LEVEL_MODIFIER)).thenReturn(PHPCS_SEVERITY_OR_LEVEL_MODIFIER);
    String severityModifier = "--level=";
    String level = "error";
    when(c.getSeverityModifier()).thenReturn(severityModifier);
    when(c.getLevel()).thenReturn(level);

    PhpCodeSnifferExecutor executor = createExecutor(settings, c);
    List<String> commandLine = executor.getCommandLineArguments();

    String expected = "--extensions=php,php2";
    assertThat(commandLine).contains(expected);
    assertThat(commandLine).contains(severityModifier + level);
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLineArguments()}.
   */
  @Test
  public void testGetCommandLineWithStandards() {
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);
    when(c.getStandard()).thenReturn("PEAR");

    PhpCodeSnifferExecutor executor = createExecutor(new Settings(), c);
    List<String> commandLine = executor.getCommandLineArguments();

    assertThat(commandLine).contains("--standard=PEAR");
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLineArguments()}.
   */
  @Test
  public void testGetCommandLineWithExtraArguments() {
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);
    when(c.getArgumentLine()).thenReturn("  --foo=bar --foo2=bar2 ");

    PhpCodeSnifferExecutor executor = createExecutor(new Settings(), c);
    List<String> commandLine = executor.getCommandLineArguments();

    assertThat(commandLine).contains("--foo=bar");
    assertThat(commandLine).contains("--foo2=bar2");
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLineArguments()}.
   */
  @Test
  public void testGetCommandLineWithDirsToAnalyse() {
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);
    File sourceDir = new File("target/fakeProject/src");
    when(c.getSourceDirectories()).thenReturn(Lists.newArrayList(sourceDir));

    PhpCodeSnifferExecutor executor = createExecutor(new Settings(), c);
    List<String> commandLine = executor.getCommandLineArguments();

    assertThat(commandLine).contains(sourceDir.getAbsolutePath());
  }

  private PhpCodeSnifferExecutor createExecutor(Settings settings, PhpCodeSnifferConfiguration c) {
    when(c.getRuleSet()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target\\logs\\php"));
    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter e = mock(PhpCodeSnifferProfileExporter.class);
    PhpCodeSnifferExecutor executor = new PhpCodeSnifferExecutor(new Php(settings), c, e, profile);
    return executor;
  }
}
