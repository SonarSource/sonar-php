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
import org.junit.Test;

public class PhpDependConfigurationTest {

  @Test
  public void shouldGetCommandLineForWindows() {
    PhpDependConfiguration config = getWindowsConfiguration();
    assertThat(config.getCommandLine(), is("pdepend.bat"));
  }

  @Test
  public void shouldGetCommandLineForNotWindows() {
    PhpDependConfiguration config = getNotWindowsConfiguration();
    assertThat(config.getCommandLine(), is("pdepend"));
  }

  @Test
  public void shouldGetCommandLineWithPath() {
    String path = "path/to/phpdepend";
    PhpDependConfiguration config = getConfiguration(false, path);
    assertThat(config.getCommandLine(), is(path + "/pdepend"));
  }

  @Test
  public void shouldGetCommandLineWithPathEvenIfExistingLastSlash() {
    String path = "path/to/phpdepend";
    PhpDependConfiguration config = getConfiguration(false, path + "/");
    assertThat(config.getCommandLine(), is(path + "/pdepend"));
  }

  private PhpDependConfiguration getWindowsConfiguration() {
    return getConfiguration(true, "");
  }

  private PhpDependConfiguration getNotWindowsConfiguration() {
    return getConfiguration(false, "");
  }

  private PhpDependConfiguration getConfiguration(final boolean isOsWindows, final String path) {
    PhpDependConfiguration config = new PhpDependConfiguration() {
      public String getPath() {
        return path;
      }

      protected boolean isOsWindows() {
        return isOsWindows;
      }
    };
    return config;
  }

}
