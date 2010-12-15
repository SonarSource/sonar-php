/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

package org.sonar.plugins.php.core.resources;

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
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpPlugin;

/**
 * The Class PhpFileTest.
 */
public class PhpFileTest {

  /** The project. */
  private Project project;

  /**
   * Constructor with null key should only set qualifier and scope.
   */
  @Test(expected = IllegalArgumentException.class)
  public void constructorWithNullKeyShouldOnlySetQualifierAndScope() {
    new PhpFile(null);
    // PhpFile phpFile = new PhpFile(null);
    // assertEquals(null, phpFile.getLanguage());
    // assertEquals(null, phpFile.getKey());
    // assertEquals(null, phpFile.getName());
    // assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
    // assertEquals(Resource.QUALIFIER_CLASS, phpFile.getQualifier());
  }

  /**
   * From absolute path in wrong path should return null.
   */
  @Test
  public void fromAbsolutePathInWrongPathShouldReturnNull() {
    init();
    assertEquals(null, PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\lib\\animal\\Monkey.php", project));
  }

  /**
   * From absolute path should initialize package and class name.
   */
  @Test
  public void fromAbsolutePathShouldInitializePackageAndClassName() {
    init();
    PhpFile phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\animal\\Monkey.php", project);
    assertEquals("animal.Monkey.php", phpFile.getKey());
    phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\insult\\Monkey.php", project);
    assertEquals("insult.Monkey.php", phpFile.getKey());
    phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\Monkey.php", project);
    assertEquals("Monkey.php", phpFile.getKey());
  }

  /**
   * From absolute path should recognize and initialize source file.
   */
  @Test
  public void fromAbsolutePathShouldRecognizeAndInitializeSourceFile() {
    init();
    PhpFile phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\animal\\Monkey.php", project);
    assertEquals(Php.INSTANCE, phpFile.getLanguage());
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
    PhpFile phpFile = PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\test\\animal\\Monkey.php", project);
    assertEquals(Php.INSTANCE, phpFile.getLanguage());
    assertEquals("animal.Monkey.php", phpFile.getKey());
    assertEquals("Monkey", phpFile.getName());
    assertEquals(Resource.QUALIFIER_FILE, phpFile.getScope());
    assertEquals(Resource.QUALIFIER_UNIT_TEST_CLASS, phpFile.getQualifier());
  }

  /**
   * From absolute path with null key should return null.
   */
  @Test
  public void fromAbsolutePathWithNullKeyShouldReturnNull() {
    assertEquals(null, PhpFile.fromAbsolutePath(null, null));
  }

  /**
   * From absolute path with wrong extension should return null.
   */
  @Test
  public void fromAbsolutePathWithWrongExtensionShouldReturnNull() {
    init();
    assertEquals(null, PhpFile.fromAbsolutePath("C:\\projets\\PHP\\Monkey\\src\\main\\animal\\Monkey.java", project));
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
    when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\src\\main")));
    when(fileSystem.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\src\\test")));
  }

}
