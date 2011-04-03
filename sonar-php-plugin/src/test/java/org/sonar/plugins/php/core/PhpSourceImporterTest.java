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

package org.sonar.plugins.php.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Project;

/**
 * Tests the basic functionality of the PhpSourceImporter.
 * 
 * @author juergen_kellerer, 2010-10-21
 * @version 1.0
 */
public class PhpSourceImporterTest {

  static List<String> sourceNames = Arrays.asList("one.php", "two.php", "three.php");
  static List<String> testNames = Arrays.asList("oneTest.php", "twoTest.php", "threeTest.php");
  static String phpCode = "<?php ?>";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private Project project;
  private SensorContext context;

  File sources, tests, testsBelowSources;

  @Before
  public void init() throws Exception {
    tempFolder.create();
    sources = tempFolder.newFolder("sources");
    tests = tempFolder.newFolder("tests");
    testsBelowSources = tempFolder.newFolder("sources/tests");

    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);

    context = mock(SensorContext.class);
    project = mock(Project.class);
    when(project.getPom()).thenReturn(new MavenProject());

    DefaultProjectFileSystem fileSystem = new DefaultProjectFileSystem(project);
    fileSystem.addSourceDir(sources);
    fileSystem.addTestDir(tests);
    fileSystem.addTestDir(testsBelowSources);

    when(project.getFileSystem()).thenReturn(fileSystem);
  }

  @Test
  public void testAnalyseAddsTestsAndSources() throws Exception {
    doTestAnalyseAddsTestsAndSources(sources, tests);
  }

  @Test
  @Ignore("Not implemented yet: Need more time to implement a better fix")
  public void testAnalyseAddsTestsAndSourcesWhenTestsAreBelowSources() throws Exception {
    doTestAnalyseAddsTestsAndSources(sources, testsBelowSources);
  }

  void doTestAnalyseAddsTestsAndSources(File sources, File tests) throws Exception {
    createFiles(sources, false);
    createFiles(tests, true);

    PhpSourceImporter importer = new PhpSourceImporter(project);
    importer.analyse(project, context);
    importer.toString();

    for (String name : sourceNames) {
      PhpFile file = PhpFile.getInstance(project).fromIOFile(new File(sources, name), Arrays.asList(sources), false);
      verify(context).saveSource(file, phpCode);
    }
    for (String name : testNames) {
      PhpFile file = PhpFile.getInstance(project).fromIOFile(new File(tests, name), Arrays.asList(tests), true);
      verify(context).saveSource(file, phpCode);
    }
    verifyNoMoreInteractions(context);
  }

  void createFiles(File path, boolean isTest) throws Exception {
    for (String s : isTest ? testNames : sourceNames) {
      FileWriter fw = new FileWriter(new File(path, s));
      fw.write(phpCode);
      fw.close();
    }
  }
}
