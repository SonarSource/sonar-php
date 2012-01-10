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

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.PhpPlugin.FILE_SUFFIXES_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_KEY;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;

import com.google.common.collect.Lists;

public class PhpmdExecutorTest {

  /**
   * Test method for {@link org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor#getCommandLine()} .
   */
  @Test
  public void testSimpleCommandLine() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty(PHPMD_REPORT_FILE_RELATIVE_PATH_KEY, "/");
    conf.setProperty(PHPMD_REPORT_FILE_NAME_KEY, "pmd.xml");
    String[] extensions = new String[] {"php", "php3", "php4"};
    conf.setProperty(FILE_SUFFIXES_KEY, extensions);
    Project project = MockUtils.createMockProject(conf);
    when(project.getExclusionPatterns()).thenReturn(new String[0]);
    PhpmdConfiguration phpmdConfiguration = new PhpmdConfiguration(project);

    PhpmdProfileExporter exporter = mock(PhpmdProfileExporter.class);
    PhpmdExecutor executor = new PhpmdExecutor(phpmdConfiguration, exporter, null);

    List<String> commandLine = executor.getCommandLine();
    String reportFile = new File("target/MockProject/target/pmd.xml").getAbsolutePath();
    String[] expected = new String[] {getSrcRelativePathAccordingToOs(), "xml", "codesize,unusedcode,naming", "--reportfile", reportFile,
      "--suffixes", StringUtils.join(extensions, ",")};

    assertThat(commandLine).isEqualTo(getExpectedCommandLineAccordingToOs(expected));
    assertThat(executor.getExecutedTool(), is("PHPMD"));
  }

  @Test
  public void testCommandLineWithSeveralParameters() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty(PHPMD_REPORT_FILE_RELATIVE_PATH_KEY, "/");
    conf.setProperty(PHPMD_REPORT_FILE_NAME_KEY, "pmd.xml");
    conf.setProperty(PHPMD_ARGUMENT_LINE_KEY, " --foo=bar  --foo2=bar2 ");
    String[] extensions = new String[] {"php", "php3", "php4"};
    conf.setProperty(FILE_SUFFIXES_KEY, extensions);
    Project project = MockUtils.createMockProject(conf);
    PhpmdConfiguration phpmdConfiguration = new PhpmdConfiguration(project);

    PhpmdProfileExporter exporter = mock(PhpmdProfileExporter.class);
    PhpmdExecutor executor = new PhpmdExecutor(phpmdConfiguration, exporter, null);

    List<String> commandLine = executor.getCommandLine();
    String reportFile = new File("target/MockProject/target/pmd.xml").getAbsolutePath();
    String[] expected = new String[] {getSrcRelativePathAccordingToOs(), "xml", "codesize,unusedcode,naming", "--reportfile", reportFile,
      "--suffixes", StringUtils.join(extensions, ","), "--foo=bar",
      "--foo2=bar2"};

    assertThat(commandLine).isEqualTo(getExpectedCommandLineAccordingToOs(expected));
  }

  protected String getSrcRelativePathAccordingToOs() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return "target\\MockProject\\src";
    } else {
      return "target/MockProject/src";
    }
  }

  private List<String> getExpectedCommandLineAccordingToOs(String[] expected) {
    if (SystemUtils.IS_OS_WINDOWS) {
      return Lists.asList("phpmd.bat", expected);
    } else {
      return Lists.asList("phpmd", expected);
    }
  }

}
