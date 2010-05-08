/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
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

package org.sonar.plugins.php.phpunit.sensor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;

public class PhpUnitExecutorTest {

  @Test
  public void shouldReturnCommandLineWithoutCoverageOptions() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));
    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    List<String> commandLine = executor.getCommandLine();
    assertTrue("Should not return any coverage options", !commandLine.contains("--coverage-clover=phpUnit.coverage.xml"));
  }

}
