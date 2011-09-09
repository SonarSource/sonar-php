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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.PhpFile;

/**
 * The Class PhpFileTest.
 */
public class PhpFileTest {

  /** The project. */
  private Project project;

  /**
   * From absolute path in wrong path should return null.
   */
  @Test
  public void fromAbsolutePathInWrongPathShouldReturnNull() {
    init();
    assertEquals(null, PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/lib/animal/Monkey.php", project));
  }

  /**
   * From absolute path should initialize package and class name.
   */
  @Test
  public void fromAbsolutePathShouldInitializePackageAndClassName() {
    init();
    PhpFile phpFile = PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/main/animal/Monkey.php", project);
    assertEquals("animal.Monkey.php", phpFile.getKey());
    phpFile = PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/main/insult/Monkey.php", project);
    assertEquals("insult.Monkey.php", phpFile.getKey());
    phpFile = PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/main/Monkey.php", project);
    assertEquals("Monkey.php", phpFile.getKey());
  }

  /**
   * From absolute path should recognize and initialize source file.
   */
  @Test
  public void fromAbsolutePathShouldRecognizeAndInitializeSourceFile() {
    init();
    PhpFile phpFile = PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/main/animal/Monkey.php", project);
    assertEquals(Php.PHP, phpFile.getLanguage());
    assertEquals("animal.Monkey.php", phpFile.getKey());
    assertEquals("Monkey", phpFile.getName());
    assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
    assertEquals(Resource.QUALIFIER_CLASS, phpFile.getQualifier());
  }

  /**
   * From absolute path should recognize and initialize test file.
   */
  @Test
  public void fromAbsolutePathShouldRecognizeAndInitializeTestFile() {
    init();
    PhpFile phpFile = PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/test/animal/Monkey.php", project);
    assertEquals(Php.PHP, phpFile.getLanguage());
    assertEquals("animal.Monkey.php", phpFile.getKey());
    assertEquals("Monkey", phpFile.getName());
    assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
    assertEquals(Resource.QUALIFIER_UNIT_TEST_CLASS, phpFile.getQualifier());
  }

  @Test
  public void fromAbsolutePathShouldRecognizeAndInitializeTestFileContainedBelowSourceDirs() {
    init();
    ProjectFileSystem fileSystem = project.getFileSystem();
    when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/src")));

    PhpFile phpFile = PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/test/animal/Monkey.php", project);
    assertThat(phpFile).isNotNull();
    assertThat(phpFile.getQualifier()).isEqualTo(Resource.QUALIFIER_UNIT_TEST_CLASS);
  }

  /**
   * From absolute path with null key should return null.
   */
  @Test
  public void fromAbsolutePathWithNullKeyShouldReturnNull() {
    assertEquals(null, PhpFile.getInstance(project).fromAbsolutePath(null, null));
  }

  /**
   * From absolute path with wrong extension should return null.
   */
  @Test
  public void fromAbsolutePathWithWrongExtensionShouldReturnNull() {
    init();
    assertEquals(null, PhpFile.getInstance(project).fromAbsolutePath("C:/projets/PHP/Monkey/src/main/animal/Monkey.java", project));
  }

  /**
   * Inits the.
   */
  private void init() {
    project = mock(Project.class);
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);

    Configuration configuration = mock(Configuration.class);
    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);

    when(project.getFileSystem()).thenReturn(fileSystem);
    when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/src/main")));
    when(fileSystem.getTestDirs()).thenReturn(Arrays.asList(new File("C:/projets/PHP/Monkey/src/test")));
    File f1 = new File("C:/projets/PHP/Monkey/src/main/insult/Monkey.php");
    File f2 = new File("C:/projets/PHP/Monkey/src/main/animal/Monkey.php");
    File f3 = new File("C:/projets/PHP/Monkey/src/main/Monkey.php");
    File f4 = new File("C:/projets/PHP/Monkey/src/test/animal/Monkey.php");
    File f5 = new File("C:/projets/PHP/Monkey/src/test/animal/Monkey.php");
    when(fileSystem.getSourceFiles(Php.PHP)).thenReturn(Arrays.asList(f1, f2, f3));
    when(fileSystem.getTestFiles(Php.PHP)).thenReturn(Arrays.asList(f4, f5));
  }
}
