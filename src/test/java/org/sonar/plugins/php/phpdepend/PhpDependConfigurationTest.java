/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpdepend;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.sonar.plugins.api.maven.model.MavenPom;

public class PhpDependConfigurationTest {

  @Test
  public void shouldGetCommandLineForWindows() {
    PhpDependConfiguration config = getWindowsConfiguration();
    assertThat(config.getCommandLine(), is(PhpDependConfiguration.COMMAND_LINE +".bat"));
  }

  @Test
  public void shouldGetCommandLineForNotWindows() {
    PhpDependConfiguration config = getNotWindowsConfiguration();
    assertThat(config.getCommandLine(), is(PhpDependConfiguration.COMMAND_LINE));
  }

  @Test
  public void shouldGetCommandLineWithPath() {
    String path = "path/to/phpdepend";
    PhpDependConfiguration config = getConfiguration(false, path);
    assertThat(config.getCommandLine(), is(path + "/"+ PhpDependConfiguration.COMMAND_LINE));
  }

  @Test
  public void shouldGetCommandLineWithPathEvenIfExistingLastSlash() {
    String path = "path/to/phpdepend";
    PhpDependConfiguration config = getConfiguration(false, path + "/");
    assertThat(config.getCommandLine(), is(path + "/"+ PhpDependConfiguration.COMMAND_LINE));
  }

  @Test
  public void shouldReportFileBeInTargetDir(){
    MavenPom pom = mock(MavenPom.class);
    PhpDependConfiguration config = new PhpDependConfiguration(pom);
    config.getReportFilecommandOption();
    verify(pom).getBuildDir();
  }

  @Test
  public void shouldGetValidSuffixeOption(){
    PhpDependConfiguration config = getWindowsConfiguration();
    String suffixesOption = config.getSuffixesCommandOption();
    assertThat(suffixesOption, notNullValue());
    assertThat(suffixesOption, containsString(","));
  }

  private PhpDependConfiguration getWindowsConfiguration() {
    return getConfiguration(true, "");
  }

  private PhpDependConfiguration getNotWindowsConfiguration() {
    return getConfiguration(false, "");
  }

  private PhpDependConfiguration getConfiguration(final boolean isOsWindows, final String path) {
    PhpDependConfiguration config = new PhpDependConfiguration() {

      protected String getCommandLinePath() {
        return path;
      }

      protected boolean isOsWindows() {
        return isOsWindows;
      }
    };
    return config;
  }

}
