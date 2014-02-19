/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.php.api.Php;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUtils {

  public static final String PHPUNIT_REPORT_DIR = "/org/sonar/plugins/php/phpunit/sensor/";
  public static final String PHPUNIT_REPORT = PHPUNIT_REPORT_DIR + "phpunit.xml";
  public static final String PHPUNIT_COVERAGE_REPORT = PHPUNIT_REPORT_DIR + "phpunit.coverage.xml";

  private MockUtils() {
  }

  public static ModuleFileSystem newMockModuleFileSystem() {
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.sourceCharset()).thenReturn(Charsets.UTF_8);
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new File("src/test/resources/PHPSquidSensor.php")));

    return fs;
  }

  public static Project newMockPHPProject() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(new Php());
    when(project.getLanguageKey()).thenReturn(Php.KEY);

    return project;
  }

  public static Project newMockJavaProject() {
    Project javaProject = mock(Project.class);
    when(javaProject.getLanguageKey()).thenReturn("java");

    return javaProject;
  }

}
