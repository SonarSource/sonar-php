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

/**
 * 
 */
package org.sonar.plugins.php;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_DEFAULT_REPORT_FILE_NAME;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

/**
 * @author akbenaissi
 * 
 */
public class MockUtils {

  public static String getFile(String path) {
    File f = new File("working");
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      return new File(path).toString();
    }
    return new File(f.getAbsoluteFile().getParent(), path).toString();
  }

  public static Project getMockProject() {
    return getMockProject("C:/projets/PHP/Monkey/sources/main");
  }

  public static Project getMockProject(String sourceDir) {
    Project project = mock(Project.class);
    Configuration c = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File(sourceDir)));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/Sources/test")));
    when(fs.getBuildDir()).thenReturn(new File("C:/projets/PHP/Monkey/target"));
    when(c.getString(PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY, PDEPEND_DEFAULT_REPORT_FILE_NAME)).thenReturn(PDEPEND_DEFAULT_REPORT_FILE_NAME);
    when(c.getString(PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn(DEFAULT_REPORT_FILE_PATH);
    when(project.getConfiguration()).thenReturn(c);
    return project;
  }

}
