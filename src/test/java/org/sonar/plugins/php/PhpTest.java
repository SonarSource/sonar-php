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

package org.sonar.plugins.php;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.sonar.plugins.php.matchers.IsPhpDirectory;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.ResourceUtils;

import java.util.Arrays;
import java.util.List;

public class PhpTest {

  @Test
  public void shouldCreateFile() {
    Resource fileUnderRoot = new File("file.php");
    assertThat(fileUnderRoot.getKey(), is("file.php"));
    assertThat(fileUnderRoot.getName(), is("file.php"));

    Resource fileUnderADirectory = new File("src/file.php");
    assertThat(fileUnderADirectory.getKey(), is("src/file.php"));
    assertThat(fileUnderADirectory.getName(), is("file.php"));

    Resource fileUnderTwoDirectory = new File("src/common/file.php");
    assertThat(fileUnderTwoDirectory.getKey(), is("src/common/file.php"));
    assertThat(fileUnderTwoDirectory.getName(), is("file.php"));
  }

  @Test
  public void shouldCreateADirectory() {
    Resource aDirectory = new Directory("src");
    assertThat(aDirectory.getKey(), is("src"));
    assertThat(aDirectory.getName(), is("src"));
  }

  @Test
  @Ignore
  public void shouldReturnParent() {
    Resource root = new Directory("root");
    assertThat(root.getParent(), nullValue());

    /*Due to SONAR-1093, this is commented out. It will have to be changed depending on the way issue is resolved
    Resource fileUnderRoot =  new File("file.php");
    assertThat(fileUnderRoot.getParent(), new IsPhpDirectory(Php.DEFAULT_DIRECTORY_NAME));
    */

    File fileUnderADirectory =  new File("src/file.php");
    // should be removed when SONAR-1094 gets fixed
    fileUnderADirectory.setLanguage(Php.INSTANCE);
    assertThat(fileUnderADirectory.getParent(), new IsPhpDirectory("src"));

    Resource fileUnderADirectory2 =  new File("src", "file.php");
    assertThat(fileUnderADirectory2.getParent(), new IsPhpDirectory("src"));

    Resource fileUnderDirectories = new File("src/common/file.php");
    assertThat(fileUnderDirectories.getParent(), new IsPhpDirectory("src/common"));
  }

  @Test
  public void shouldResolveFileFromAbsolutePath() {
    List<java.io.File> sources = Arrays.asList(new java.io.File("/usr/local/sources/"), new java.io.File("/home/project/src/"));

    Resource file = File.fromIOFile(new java.io.File("/home/project/src/MyFile.php"), sources);
    assertThat(file.getKey(), is("MyFile.php"));
    assertThat(file.getName(), is("MyFile.php"));
    assertTrue(ResourceUtils.isFile(file));
    // issue SONAR-1093
    // assertThat(file.getParent(), new IsPhpDirectory(Php.DEFAULT_DIRECTORY_NAME));

    Resource fileUnderDir = File.fromIOFile(new java.io.File("/home/project/src/common/MyFile.php"), sources);
    assertThat(fileUnderDir.getKey(), is("common/MyFile.php"));
    assertThat(fileUnderDir.getName(), is("MyFile.php"));
    assertTrue(ResourceUtils.isFile(file));

    // issue SONAR-1094
    //assertThat(fileUnderDir.getParent(), new IsPhpDirectory("common"));
  }

  @Test
  public void shouldConvertBackSlashToSlashWhenResolvingFileFromAbsolutePath() {
    List<java.io.File> sources = Arrays.asList(new java.io.File("c:\\project\\php\\test"));

    Resource fileUnderDir = File.fromIOFile(new java.io.File("c:\\project\\php\\test\\src\\common\\MyFile.php"), sources);
    assertThat(fileUnderDir.getKey(), is("src/common/MyFile.php"));
    assertThat(fileUnderDir.getName(), is("MyFile.php"));
    // issue SONAR-1094
    //    assertThat(fileUnderDir.getParent(), new IsPhpDirectory("src/common"));
  }

  @Test
  public void shouldCheckValidPhpExtensions() {
    assertTrue(Php.containsValidSuffixes("goodExtension.php"));
    assertTrue(Php.containsValidSuffixes("goodExtension.php5"));
    assertFalse(Php.containsValidSuffixes("wrong.extension"));
  }

  @Test
  public void shouldResolveUnitTestFileFromAbsolutePath() {
    List<java.io.File> sources = Arrays.asList(new java.io.File("/home/project/test/"));

    File file = File.fromIOFile(new java.io.File("/home/project/test/MyFileTest.php"), sources);
    file.setQualifier(Resource.QUALIFIER_UNIT_TEST_CLASS);

    assertThat(file.getKey(), is("MyFileTest.php"));
    assertThat(file.getName(), is("MyFileTest.php"));
    assertTrue(ResourceUtils.isUnitTestClass(file));

    Resource fileUnderDir = File.fromIOFile(new java.io.File("/home/project/test/common/MyFileTest.php"), sources);
    assertThat(fileUnderDir.getKey(), is("common/MyFileTest.php"));
    assertThat(fileUnderDir.getName(), is("MyFileTest.php"));
    assertTrue(ResourceUtils.isUnitTestClass(file));
  }

}
