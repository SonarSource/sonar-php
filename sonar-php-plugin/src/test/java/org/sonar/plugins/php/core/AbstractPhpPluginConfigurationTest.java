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

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;

public class AbstractPhpPluginConfigurationTest {

  @Test
  public void shouldNotSkipWhenShouldRunAndSkipNotSet() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(false, conf.isSkip());
  }

  @Test
  public void shouldSkipWhenShouldRunSetToFalseAndSkipNotSet() {
    Configuration config = new BaseConfiguration();
    config.setProperty("run", false);

    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(true, conf.isSkip());
  }

  @Test
  public void shouldNotSkipWhenShouldRunSetToTrueAndSkipNotSet() {
    Configuration config = new BaseConfiguration();
    config.setProperty("run", true);

    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(false, conf.isSkip());
  }

  @Test
  public void shouldSkipWhenSkipSetToTrueAndShouldRunNotSet() {
    Configuration config = new BaseConfiguration();
    config.setProperty("skip", true);

    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(true, conf.isSkip());
  }

  @Test
  public void shouldNotSkipWhenSkipSetToFalseAndShouldRunNotSet() {
    Configuration config = new BaseConfiguration();
    config.setProperty("skip", false);

    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(false, conf.isSkip());
  }

  @Test
  public void shouldSkipIgnoringShouldRunValueWhenSkipSetToTrue() {
    Configuration config = new BaseConfiguration();
    config.setProperty("skip", true);
    config.setProperty("run", true);

    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(true, conf.isSkip());
  }

  @Test
  public void shouldNotSkipIgnoringShouldRunValueWhenSkipSetToFalse() {
    Configuration config = new BaseConfiguration();
    config.setProperty("skip", false);
    config.setProperty("run", true);

    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);

    assertEquals(false, conf.isSkip());
  }

  @Test
  public void testDynamicAnalysisProperty() {
    Configuration config = new BaseConfiguration();
    Project project = MockUtils.createMockProject(config);
    FakeConfiguration conf = new FakeConfiguration(project);
    assertEquals(true, conf.isDynamicAnalysisEnabled());

    // Set to FALSE
    config.setProperty("sonar.dynamicAnalysis", "false");
    conf = new FakeConfiguration(project);
    assertEquals(false, conf.isDynamicAnalysisEnabled());

    // Set to REUSE REPORTS (may be possible in Sonar, does not make sense for the moment in PHP plugin but must not break)
    config.setProperty("sonar.dynamicAnalysis", "reuseReports");
    conf = new FakeConfiguration(project);
    assertEquals(true, conf.isDynamicAnalysisEnabled());
  }

  class FakeConfiguration extends AbstractPhpConfiguration {

    public FakeConfiguration(Project project) {
      super(project);
    }

    @Override
    protected String getArgumentLineKey() {
      return "";
    }

    @Override
    protected String getCommandLine() {
      return "";
    }

    @Override
    protected String getDefaultArgumentLine() {
      return "";
    }

    @Override
    protected String getDefaultReportFileName() {
      return "";
    }

    @Override
    protected String getDefaultReportFilePath() {
      return "";
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
    protected String getTimeoutKey() {
      return "";
    }

    @Override
    protected String getShouldAnalyzeOnlyKey() {
      return "analyze";
    }

    @Override
    protected String getSkipKey() {
      return "skip";
    }

    @Override
    protected String getShouldRunKey() {
      return "run";
    }

  }

}
