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

package org.sonar.plugins.php.pmd;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.api.CoreProperties.PROJECT_EXCLUSIONS_PROPERTY;
import static org.sonar.plugins.php.MockUtils.getFile;
import static org.sonar.plugins.php.MockUtils.getMockProject;
import static org.sonar.plugins.php.core.PhpPlugin.FILE_SUFFIXES_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_NAME;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_PATH;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_IGNORE_ARGUMENT_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_PROPERTY_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.api.resources.Project;

public class PhpmdExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()} .
   */
  @Test
  public void testGetCommandLine1() {
    Project project = getMockProject();
    Configuration configuration = project.getConfiguration();
    String[] extensions = new String[] { "php", "php3", "php4" };
    when(configuration.getStringArray(FILE_SUFFIXES_KEY)).thenReturn(extensions);
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn("/");
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn("pmd.xml");

    PhpmdConfiguration c = getWindowsConfiguration(project);
    PhpmdProfileExporter e = mock(PhpmdProfileExporter.class);
    PhpmdExecutor executor = new PhpmdExecutor(c, e, null);

    List<String> commandLine = executor.getCommandLine();

    String f1 = new File("C:/projets/PHP/Monkey/sources/main").toString();
    String f2 = new File("C:/projets/PHP/Monkey/target/pmd.xml").toString();
    String[] expected = new String[] { "phpmd.bat", f1, "xml", "codesize,unusedcode,naming", "--reportfile", f2, "--extensions",
      StringUtils.join(extensions, ",") };

    assertThat(commandLine).isEqualTo(Arrays.asList(expected));
  }

  @Test
  public void testGetIgnoreDirsWithNotNullWithSonarExclusionNull() {
    Project project = getMockProject();
    Configuration configuration = project.getConfiguration();
    String[] extensions = new String[] { "php", "php3", "php4" };
    when(configuration.getStringArray(FILE_SUFFIXES_KEY)).thenReturn(extensions);
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn("/");
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn("pmd.xml");

    PhpmdConfiguration c = getWindowsConfiguration(project);
    PhpmdProfileExporter e = mock(PhpmdProfileExporter.class);

    when(c.isStringPropertySet(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(true);
    String pdependExclusionPattern = "Math,Math3*";
    when(configuration.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(new String[] { pdependExclusionPattern });

    when(configuration.getStringArray(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(null);

    assertThat(c.getIgnoreList()).isEqualTo(pdependExclusionPattern);
    PhpmdExecutor executor = new PhpmdExecutor(c, e, null);
    List<String> commandLine = executor.getCommandLine();

    String f1 = new File("C:/projets/PHP/Monkey/sources/main").toString();
    String f2 = new File("C:/projets/PHP/Monkey/target/pmd.xml").toString();
    String[] expected = new String[] { "phpmd.bat", f1, "xml", "codesize,unusedcode,naming", "--reportfile", f2, "--extensions",
      StringUtils.join(extensions, ",") };
    assertThat(commandLine).isEqualTo(Arrays.asList(expected));
  }

  @Test
  public void testGetIgnoreDirsNullWithSonarExclusionNotNull() {
    Project project = getMockProject();
    Configuration configuration = project.getConfiguration();
    String[] extensions = new String[] { "php", "php3", "php4" };
    when(configuration.getStringArray(FILE_SUFFIXES_KEY)).thenReturn(extensions);
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn("/");
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn("pmd.xml");

    PhpmdConfiguration c = getWindowsConfiguration(project);
    PhpmdProfileExporter e = mock(PhpmdProfileExporter.class);

    when(c.isStringPropertySet(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(false);
    when(configuration.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(null);

    when(c.isStringPropertySet(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(true);
    String[] sonarExclusionPattern = { "*test", "**/math" };
    when(configuration.getStringArray(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(sonarExclusionPattern);

    PhpmdExecutor executor = new PhpmdExecutor(c, e, null);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "phpmd.bat";
    String s2 = getFile("C:/projets/PHP/Monkey/sources/main");
    String s3 = "xml";
    String s4 = "codesize,unusedcode,naming";
    String s5 = "--reportfile";
    String s6 = new File("C:/projets/PHP/Monkey/target/pmd.xml").toString();
    String s7 = "--ignore";
    String s8 = StringUtils.join(sonarExclusionPattern, ",");
    String s9 = "--extensions";
    String s10 = "php,php3,php4";

    List<String> expected = Arrays.asList(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10);

    assertThat(commandLine).isEqualTo(expected);
  }

  @Test
  public void testGetIgnoreDirsNotNullWithSonarExclusionNotNull() {
    Project project = getMockProject();
    Configuration configuration = project.getConfiguration();
    String[] extensions = new String[] { "php", "php3", "php4" };
    when(configuration.getStringArray(FILE_SUFFIXES_KEY)).thenReturn(extensions);
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn("/");
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn("pmd.xml");

    PhpmdConfiguration c = getWindowsConfiguration(project);
    PhpmdProfileExporter e = mock(PhpmdProfileExporter.class);

    when(c.isStringPropertySet(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(true);
    String[] phpmdExclusionPattern = { "*Math5.php" };
    when(configuration.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(phpmdExclusionPattern);

    when(c.isStringPropertySet(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(true);
    String[] sonarExclusionPattern = { "sites/all/", "files", "*Math4.php" };
    when(configuration.getStringArray(PROJECT_EXCLUSIONS_PROPERTY)).thenReturn(sonarExclusionPattern);

    PhpmdExecutor executor = new PhpmdExecutor(c, e, null);
    List<String> commandLine = executor.getCommandLine();
    String s1 = "phpmd.bat";
    String s2 = getFile("C:/projets/PHP/Monkey/sources/main");
    String s3 = "xml";
    String s4 = "codesize,unusedcode,naming";
    String s5 = "--reportfile";
    String s6 = new File("C:/projets/PHP/Monkey/target/pmd.xml").toString();
    String s7 = "--ignore";
    String s8 = StringUtils.join(phpmdExclusionPattern, ",");
    s8 += "," + StringUtils.join(sonarExclusionPattern, ",");
    String s9 = "--extensions";
    String s10 = "php,php3,php4";

    List<String> expected = Arrays.asList(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10);

    assertThat(commandLine).isEqualTo(expected);
    assertThat(commandLine).isEqualTo(expected);
  }

  /**
   * Gets the windows configuration.
   * 
   * @return the windows configuration
   */
  private PhpmdConfiguration getWindowsConfiguration(Project project) {
    return getConfiguration(project, true, "aaa");
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

  private PhpmdConfiguration getConfiguration(Project project, final boolean isOsWindows, final String path) {
    PhpmdConfiguration config = new PhpmdConfiguration(project) {

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
