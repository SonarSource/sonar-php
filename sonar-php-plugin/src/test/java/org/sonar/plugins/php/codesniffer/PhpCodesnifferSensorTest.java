/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SHOULD_RUN_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SKIP_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.ActiveRule;
import org.sonar.plugins.php.core.Php;

/**
 * The Class PhpCodesnifferSensorTest.
 */
public class PhpCodesnifferSensorTest {

  @Test
  public void shouldNotLaunchOnNonPhpProject() {

    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Java.INSTANCE);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, null);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchWhenShouldRunSetToFalseAndSkipNotSet() {

    Project project = mock(Project.class);
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(PHPCS_SHOULD_RUN_KEY, false);

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, null);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchWhenShouldRunSetToTrueAndSkipNotSet() {

    Project project = mock(Project.class);
    RulesProfile profile = mock(RulesProfile.class);

    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(PHPCS_SHOULD_RUN_KEY, true);

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getReuseExistingRulesConfig()).thenReturn(true);

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(mock(ActiveRule.class));
    when(profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY)).thenReturn(activeRules);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, profile);
    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchWhenSkipSetToTrueAndShouldRunNotSet() {

    Project project = mock(Project.class);
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(PHPCS_SKIP_KEY, true);

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, null);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchWhenSkipSetToFalseAndShouldRunNotSet() {

    Project project = mock(Project.class);
    RulesProfile profile = mock(RulesProfile.class);
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(PHPCS_SKIP_KEY, false);

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getReuseExistingRulesConfig()).thenReturn(true);

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(mock(ActiveRule.class));
    when(profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY)).thenReturn(activeRules);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, profile);
    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchIgnoringShouldRunValueWhenSkipSetToTrue() {

    Project project = mock(Project.class);
    RulesProfile profile = mock(RulesProfile.class);
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(PHPCS_SKIP_KEY, true);
    config.setProperty(PHPCS_SHOULD_RUN_KEY, false);

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getReuseExistingRulesConfig()).thenReturn(true);

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(mock(ActiveRule.class));
    when(profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY)).thenReturn(activeRules);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, profile);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchIgnoringShouldRunValueWhenSkipSetToFalse() {

    Project project = mock(Project.class);
    RulesProfile profile = mock(RulesProfile.class);
    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setProperty(PHPCS_SKIP_KEY, false);
    config.setProperty(PHPCS_SHOULD_RUN_KEY, true);

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getReuseExistingRulesConfig()).thenReturn(true);

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(mock(ActiveRule.class));
    when(profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY)).thenReturn(activeRules);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, profile);
    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchByDefaultWhenShouldRunAndSkipNotSet() {

    Project project = mock(Project.class);
    RulesProfile profile = mock(RulesProfile.class);
    PropertiesConfiguration config = new PropertiesConfiguration();

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getReuseExistingRulesConfig()).thenReturn(true);

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(mock(ActiveRule.class));
    when(profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY)).thenReturn(activeRules);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, profile);
    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchIfNotReusingExistingRulesAndNoActiveRulesPresent() {

    Project project = mock(Project.class);
    RulesProfile profile = mock(RulesProfile.class);
    PropertiesConfiguration config = new PropertiesConfiguration();

    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getReuseExistingRulesConfig()).thenReturn(false);

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(null, null, profile);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

}
