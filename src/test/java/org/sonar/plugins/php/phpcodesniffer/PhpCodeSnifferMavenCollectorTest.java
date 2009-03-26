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

package org.sonar.plugins.php.phpcodesniffer;

import org.apache.commons.io.FileUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.commons.rules.RulesProfile;

import java.io.File;
import java.io.IOException;

public class PhpCodeSnifferMavenCollectorTest {

  private PhpCodeSnifferConfiguration phpCodeSnifferConfiguration;
  private PhpCodeSnifferRulesRepository rulesRepository;
  private RulesProfile rulesProfile;


  @Before
  public void before() {
    phpCodeSnifferConfiguration = mock(PhpCodeSnifferConfiguration.class);
    rulesRepository = mock(PhpCodeSnifferRulesRepository.class);
    rulesProfile = mock(RulesProfile.class);

    File profileDir = new File("target");
    when(phpCodeSnifferConfiguration.getDirProfile()).thenReturn(profileDir);
    File configurationFile = new File(profileDir, "configuration.php");
    when(phpCodeSnifferConfiguration.getFileProfile()).thenReturn(configurationFile);
  }

  @Test
  public void shouldCreateFileProfile() {
    File profileDir = new File("target");
    when(phpCodeSnifferConfiguration.getDirProfile()).thenReturn(profileDir);
    File configurationFile = new File(profileDir, "configuration.php");
    when(phpCodeSnifferConfiguration.getFileProfile()).thenReturn(configurationFile);

    when(rulesRepository.exportConfiguration(eq(rulesProfile), anyString())).thenReturn("");

    PhpCodeSnifferMavenCollector collector = new PhpCodeSnifferMavenCollector(rulesProfile, rulesRepository);
    collector.createRulesConfigurationFolderAndFile(phpCodeSnifferConfiguration);

    assertTrue(configurationFile.exists());
    assertThat(configurationFile.getParent(), endsWith(profileDir.getName()));
  }

  @Test
  public void shouldWriteRulesConfigurationContentToFile() throws IOException {
    File profileDir = new File("target");
    when(phpCodeSnifferConfiguration.getDirProfile()).thenReturn(profileDir);
    File configurationFile = new File(profileDir, "configuration.php");
    when(phpCodeSnifferConfiguration.getFileProfile()).thenReturn(configurationFile);

    String configurationContent = "a configuration content";
    when(rulesRepository.exportConfiguration(eq(rulesProfile), anyString())).thenReturn(configurationContent);

    PhpCodeSnifferMavenCollector collector = new PhpCodeSnifferMavenCollector(rulesProfile, rulesRepository);
    collector.createRulesConfigurationFolderAndFile(phpCodeSnifferConfiguration);

    assertThat(FileUtils.readFileToString(configurationFile, "UTF-8"), is(configurationContent));
  }

}
