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

package org.sonar.plugins.php.phpdepend;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.api.CoreProperties.PROJECT_EXCLUSIONS_PROPERTY;
import static org.sonar.plugins.php.MockUtils.getFile;
import static org.sonar.plugins.php.MockUtils.getMockProject;
import static org.sonar.plugins.php.core.PhpPlugin.FILE_SUFFIXES_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_IGNORE_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.api.resources.Project;

public class PhpDependExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()} .
   */
  @Test
  public void testGetCommandLine1() {
    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(FILE_SUFFIXES_KEY)).thenReturn(null);
    Project project = getMockProject();
    PhpDependConfiguration c = getWindowsConfiguration(project);
    PhpDependExecutor executor = new PhpDependExecutor(c);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "pdepend.bat";
    String s2 = "--phpunit-xml=" + getFile("C:/projets/PHP/Monkey/target/logs/pdepend.xml");
    String s3 = "--suffix=php,php3,php4,php5,phtml,inc";
    String s4 = new File("C:/projets/PHP/Monkey/sources/main").toString();
    List<String> expected = Arrays.asList(s1, s2, s3, s4);
    //
    assertThat(commandLine).isEqualTo(expected);
  }

  @Test
  public void testGetIgnoreDirsWithNotNullWithSonarExclusionNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();

    when(config.isStringPropertySet(PDEPEND_IGNORE_KEY)).thenReturn(true);
    String pdependExclusionPattern = "Math,Math3*";
    when(c.getStringArray(PDEPEND_IGNORE_KEY)).thenReturn(new String[] { pdependExclusionPattern });

    when(c.getStringArray(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(null);

    assertThat(config.getIgnoreDirs()).isEqualTo(pdependExclusionPattern);
    PhpDependExecutor executor = new PhpDependExecutor(config);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "pdepend.bat";
    String s2 = "--phpunit-xml=" + getFile("C:/projets/PHP/Monkey/target/logs/pdepend.xml");
    String s3 = "--suffix=php,php3,php4,php5,phtml,inc";
    String s4 = "--ignore=" + pdependExclusionPattern;
    String s5 = new File("C:/projets/PHP/Monkey/sources/main").toString();

    List<String> expected = Arrays.asList(s1, s2, s3, s4, s5);
    assertThat(commandLine).isEqualTo(expected);
  }

  @Test
  public void testGetIgnoreDirsNullWithSonarExclusionNotNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();

    when(config.isStringPropertySet(PDEPEND_IGNORE_KEY)).thenReturn(false);
    when(c.getStringArray(PDEPEND_IGNORE_KEY)).thenReturn(null);

    when(config.isStringPropertySet(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(true);
    String[] sonarExclusionPattern = { "*test", "**/math" };
    when(c.getStringArray(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(sonarExclusionPattern);

    PhpDependExecutor executor = new PhpDependExecutor(config);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "pdepend.bat";
    String s2 = "--phpunit-xml=" + getFile("C:/projets/PHP/Monkey/target/logs/pdepend.xml");
    String s3 = "--suffix=php,php3,php4,php5,phtml,inc";
    String s4 = "--ignore=" + StringUtils.join(sonarExclusionPattern, ",");
    String s5 = new File("C:/projets/PHP/Monkey/sources/main").toString();

    List<String> expected = Arrays.asList(s1, s2, s3, s4, s5);

    assertThat(commandLine).isEqualTo(expected);
  }

  @Test
  public void testGetIgnoreDirsNotNullWithSonarExclusionNotNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();

    when(config.isStringPropertySet(PDEPEND_IGNORE_KEY)).thenReturn(true);
    String[] pdependExclusionPattern = { "*Math4.php" };
    when(c.getStringArray(PDEPEND_IGNORE_KEY)).thenReturn(pdependExclusionPattern);

    when(config.isStringPropertySet(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(true);
    String[] sonarExclusionPattern = { "sites/all/", "files", "*Math4.php" };
    when(c.getStringArray(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(sonarExclusionPattern);

    PhpDependExecutor executor = new PhpDependExecutor(config);
    List<String> commandLine = executor.getCommandLine();

    String s1 = "pdepend.bat";
    String s2 = "--phpunit-xml=" + getFile("C:/projets/PHP/Monkey/target/logs/pdepend.xml");
    String s3 = "--suffix=php,php3,php4,php5,phtml,inc";
    String s4 = "--ignore=" + StringUtils.join(pdependExclusionPattern, ",") + "," + StringUtils.join(sonarExclusionPattern, ",");
    String s5 = new File("C:/projets/PHP/Monkey/sources/main").toString();
    List<String> expected = Arrays.asList(s1, s2, s3, s4, s5);

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
