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

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.MockUtils.getFile;
import static org.sonar.plugins.php.MockUtils.getMockProject;
import static org.sonar.plugins.php.PhpPlugin.FILE_SUFFIXES_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;

public class PhpDependExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()} .
   */
  @Test
  public void testSimpleCommandLine() {
    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(FILE_SUFFIXES_KEY)).thenReturn(null);
    Project project = getMockProject();
    when(project.getExclusionPatterns()).thenReturn(new String[0]);
    PhpDependConfiguration c = getWindowsConfiguration(project);
    PhpDependExecutor executor = new PhpDependExecutor(c);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "pdepend.bat";
    String s2 = "--phpunit-xml=" + getFile("C:/projets/PHP/Monkey/target/logs/pdepend.xml");
    String s3 = "--suffix=php,php3,php4,php5,phtml,inc";
    String s4 = new File("C:/projets/PHP/Monkey/sources/main").toString();
    List<String> expected = Arrays.asList(s1, s2, s3, s4);

    assertThat(commandLine).isEqualTo(expected);
    assertThat(executor.getExecutedTool(), is("PHP Depend"));
  }

  @Test
  public void testCommandLineWithSeveralParameters() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty(PDEPEND_EXCLUDE_PACKAGE_KEY, "foo,bar");
    conf.setProperty(PDEPEND_WITHOUT_ANNOTATION_KEY, "true");
    conf.setProperty(PDEPEND_ARGUMENT_LINE_KEY, "  --foo=bar --foo2=bar2 ");
    Project project = MockUtils.createMockProject(conf);
    PhpDependConfiguration config = getWindowsConfiguration(project);

    PhpDependExecutor executor = new PhpDependExecutor(config);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "pdepend.bat";
    String s2 = "--phpunit-xml=" + new File("target/MockProject/target/logs/pdepend.xml").getAbsolutePath();
    String s3 = "--suffix=php,php3,php4,php5,phtml,inc";
    String s4 = "--exclude=foo,bar";
    String s5 = "--without-annotations";
    String s6 = "--foo=bar";
    String s7 = "--foo2=bar2";
    String s8 = new File("target/MockProject/src").toString();

    List<String> expected = Arrays.asList(s1, s2, s3, s4, s5, s6, s7, s8);

    assertThat(commandLine).isEqualTo(expected);
  }

  /**
   * Gets the windows configuration.
   * 
   * @return the windows configuration
   */
  private PhpDependConfiguration getWindowsConfiguration(Project project) {
    return getConfiguration(project, true, "");
  }

  /**
   * Gets the configuration.
   * 
   * @param isOsWindows
   *          the is os windows
   * @param path
   *          the path
   * @return the configuration
   */

  private PhpDependConfiguration getConfiguration(Project project, final boolean isOsWindows, final String path) {
    PhpDependConfiguration config = new PhpDependConfiguration(project) {

      @SuppressWarnings("unused")
      public String getCommandLinePath() {
        return path;
      }

      @Override
      public boolean isOsWindows() {
        return isOsWindows;
      }
    };
    return config;
  }

}
