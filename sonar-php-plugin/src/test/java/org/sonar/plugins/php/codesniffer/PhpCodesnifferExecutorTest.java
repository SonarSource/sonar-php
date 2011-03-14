/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.PhpPlugin;

/**
 * @author akram
 * 
 */
public class PhpCodesnifferExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()}.
   */
  @Test
  public void testGetCommandLine1() {
    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);
    Project p = mock(Project.class);
    when(p.getConfiguration()).thenReturn(configuration);
    when(c.getProject()).thenReturn(p);
    when(c.getRuleSet()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target\\logs\\php"));

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter e = mock(PhpCodeSnifferProfileExporter.class);

    PhpCodeSnifferExecutor executor = new PhpCodeSnifferExecutor(c, e, profile);
    executor.getCommandLine();
  }

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()}.
   */
  @Test
  public void testGetCommandLine2() {
    Configuration configuration = mock(Configuration.class);
    String[] suffixes = new String[] { "php", "php2" };
    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(suffixes);
    PhpCodeSnifferConfiguration c = mock(PhpCodeSnifferConfiguration.class);
    Project p = mock(Project.class);
    when(p.getConfiguration()).thenReturn(configuration);
    when(c.getProject()).thenReturn(p);
    when(c.getRuleSet()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target\\logs\\php"));

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter e = mock(PhpCodeSnifferProfileExporter.class);
    PhpCodeSnifferExecutor executor = new PhpCodeSnifferExecutor(c, e, profile);

    List<String> commandLine = executor.getCommandLine();
    String expected = "--extensions=php,php2";
    assertThat(commandLine).contains(expected);
  }
}
