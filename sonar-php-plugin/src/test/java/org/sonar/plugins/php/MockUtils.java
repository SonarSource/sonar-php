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
/**
 *
 */
package org.sonar.plugins.php;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUtils {

  public static final String PHPUNIT_REPORT_DIR = "/org/sonar/plugins/php/phpunit/sensor/";
  public static final String PHPUNIT_REPORT = PHPUNIT_REPORT_DIR + "phpunit.xml";
  public static final String PHPUNIT_COVERAGE_REPORT = PHPUNIT_REPORT_DIR + "phpunit.coverage.xml";

  private MockUtils() {
  }

  public static Project newMockPHPProject() {
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(fs.getSourceCharset()).thenReturn(Charsets.UTF_8);
    when(fs.getSourceDirs()).thenReturn(ImmutableList.of(new File("src/test/resources/")));
    when(fs.mainFiles(Php.KEY)).thenReturn(ImmutableList.of(InputFileUtils.create(new File("src/test/resources/"), new File("src/test/resources/PHPSquidSensor.php"))));

    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(new Php());
    when(project.getLanguageKey()).thenReturn(Php.KEY);
    when(project.getFileSystem()).thenReturn(fs);

    return project;
  }

  public static Project newMockJavaProject() {
    Project javaProject = mock(Project.class);
    when(javaProject.getLanguageKey()).thenReturn("java");

    return javaProject;
  }

}
