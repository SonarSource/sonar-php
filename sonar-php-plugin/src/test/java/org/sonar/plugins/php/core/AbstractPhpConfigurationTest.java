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
package org.sonar.plugins.php.core;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.plugins.php.MockUtils;

import static org.fest.assertions.Assertions.assertThat;

public class AbstractPhpConfigurationTest {

  @Test
  public void shouldNotSkipWhenShouldRunAndSkipNotSet() {
    FakeConfiguration conf = new FakeConfiguration(new Settings());

    if (SystemUtils.IS_OS_WINDOWS) {
      assertThat(conf.getOsDependentToolScriptName()).isEqualTo("fake-exec.bat");
    } else {
      assertThat(conf.getOsDependentToolScriptName()).isEqualTo("fake-exec");
    }
  }

  @Test
  public void testIsDynamicAnalysisEnabled() {
    Settings settings = new Settings();
    FakeConfiguration conf = new FakeConfiguration(settings);
    assertThat(conf.isDynamicAnalysisEnabled()).isTrue();

    settings.setProperty("sonar.dynamicAnalysis", "false");
    conf = new FakeConfiguration(settings);
    assertThat(conf.isDynamicAnalysisEnabled()).isFalse();
  }

  @Test
  public void testOldParamShouldRun() {
    Settings settings = new Settings();
    FakeConfiguration conf = new FakeConfiguration(settings);
    assertThat(conf.isSkip()).isFalse();

    // one version
    settings.setProperty("shouldRun", "false");
    conf = new FakeConfiguration(settings);
    assertThat(conf.isSkip()).isTrue();

    // and the contrary
    settings.setProperty("shouldRun", "true");
    conf = new FakeConfiguration(settings);
    assertThat(conf.isSkip()).isFalse();
  }

  class FakeConfiguration extends AbstractPhpConfiguration {

    public FakeConfiguration(Settings settings) {
      super(settings, MockUtils.createMockProject().getFileSystem());
    }

    @Override
    protected String getArgumentLineKey() {
      return "";
    }

    @Override
    protected String getCommandLine() {
      return "fake-exec";
    }

    @Override
    protected String getReportFileNameKey() {
      return "";
    }

    @Override
    protected String getReportFileRelativePathKey() {
      return "";
    }

    @Override
    protected String getReportPathKey() {
      return "";
    }

    @Override
    protected String getTimeoutKey() {
      return "";
    }

    @Override
    protected String getShouldAnalyzeOnlyKey() {
      return "";
    }

    @Override
    protected String getSkipKey() {
      return "";
    }

    @Override
    protected String getShouldRunKey() {
      return "shouldRun";
    }

  }

}
