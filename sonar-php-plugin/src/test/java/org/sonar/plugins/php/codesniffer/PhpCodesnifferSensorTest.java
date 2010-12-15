/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 EchoSource
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

package org.sonar.plugins.php.codesniffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.Php;

/**
 * The Class PhpCodesnifferSensorTest.
 */
public class PhpCodesnifferSensorTest {

  @Before
  public void setUp() {
    // new Php();
  }

  /**
   * Sould not launche on non php project.
   */
  @Test
  public void shouldNotLauncheOnNonPhpProject() {
    Project project = mock(Project.class);
    Configuration config = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(
        config.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_PROPERTY_KEY,
            PhpCodeSnifferConfiguration.PHPCS_DEFAULT_REPORT_FILE_NAME)).thenReturn("tot.xml");
    when(
        config.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpCodeSnifferConfiguration.PHPCS_DEFAULT_REPORT_FILE_PATH)).thenReturn(
        PhpCodeSnifferConfiguration.PHPCS_DEFAULT_REPORT_FILE_PATH);
    when(config.getBoolean(PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_KEY, false)).thenReturn(true);
    when(project.getConfiguration()).thenReturn(config);
    when(project.getLanguage()).thenReturn(Php.INSTANCE);

    PhpCodeSnifferExecutor executor = mock(PhpCodeSnifferExecutor.class);
    PhpCodeSnifferConfiguration configuration = mock(PhpCodeSnifferConfiguration.class);
    PhpCodeSnifferViolationsXmlParser parser = mock(PhpCodeSnifferViolationsXmlParser.class);
    when(executor.getConfiguration()).thenReturn(configuration);
    when(configuration.getRuleSet()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target\\logs\\php"));

    PhpCodesnifferSensor sensor = new PhpCodesnifferSensor(executor, parser);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }
}
