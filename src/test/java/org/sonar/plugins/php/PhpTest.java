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
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.php.matchers.IsPhpDirectory;

import java.util.Arrays;
import java.util.List;

public class PhpTest {

  @Test
  public void shouldCreateFile() {
    Resource fileUnderRoot = Php.newFile("file.php");
    assertThat(fileUnderRoot.getKey(), is("file.php"));
    assertThat(fileUnderRoot.getName(), is("file.php"));

    Resource fileUnderADirectory = Php.newFile("src/file.php");
    assertThat(fileUnderADirectory.getKey(), is("src/file.php"));
    assertThat(fileUnderADirectory.getName(), is("file.php"));

    Resource fileUnderTwoDirectory = Php.newFile("src/common/file.php");
    assertThat(fileUnderTwoDirectory.getKey(), is("src/common/file.php"));
    assertThat(fileUnderTwoDirectory.getName(), is("file.php"));
  }

  @Test
  public void shouldCreateADirectory() {
    Resource aDirectory = Php.newDirectory("src");
    assertThat(aDirectory.getKey(), is("src"));
    assertThat(aDirectory.getName(), is("src"));
  }

  @Test
  public void shouldReturnParent() {
    Resource root = Php.newDirectory("root");
    assertThat(new Php().getParent(root), nullValue());

    Resource fileUnderRoot = Php.newFile("file.php");
    assertThat(new Php().getParent(fileUnderRoot), nullValue());

    Resource fileUnderADirectory = Php.newFile("src/file.php");
    assertThat(new Php().getParent(fileUnderADirectory), new IsPhpDirectory("src"));

    Resource fileUnderADirectory2 = Php.newFile("src", "file.php");
    assertThat(new Php().getParent(fileUnderADirectory2), new IsPhpDirectory("src"));

    Resource fileUnderDirectories = Php.newFile("src/common/file.php");
    assertThat(new Php().getParent(fileUnderDirectories), new IsPhpDirectory("src/common"));
  }

  @Test
  public void shouldResolveFileFromAbsolutePath() {
    List<String> sources = Arrays.asList("/usr/local/sources/", "/home/project/src/");

    Resource file = Php.newFileFromAbsolutePath("/home/project/src/MyFile.php", sources);
    assertThat(file.getKey(), is("MyFile.php"));
    assertThat(file.getName(), is("MyFile.php"));
    assertThat(new Php().getParent(file), nullValue());

    Resource fileUnderDir = Php.newFileFromAbsolutePath("/home/project/src/common/MyFile.php", sources);
    assertThat(fileUnderDir.getKey(), is("common/MyFile.php"));
    assertThat(fileUnderDir.getName(), is("MyFile.php"));
    assertThat(new Php().getParent(fileUnderDir), new IsPhpDirectory("common"));
  }

  @Test
  public void shouldConvertBackSlashToSlashWhenResolvingFileFromAbsolutePath() {
    List<String> sources = Arrays.asList("c:\\project\\php\\test");

    Resource fileUnderDir = Php.newFileFromAbsolutePath("c:\\project\\php\\test\\src\\common\\MyFile.php", sources);
    assertThat(fileUnderDir.getKey(), is("src/common/MyFile.php"));
    assertThat(fileUnderDir.getName(), is("MyFile.php"));
    assertThat(new Php().getParent(fileUnderDir), new IsPhpDirectory("src/common"));
  }

  @Test
  public void shouldCheckValidPhpExtensions() {
    assertTrue(Php.containsValidSuffixes("goodExtension.php"));
    assertTrue(Php.containsValidSuffixes("goodExtension.php5"));
    assertFalse(Php.containsValidSuffixes("wrong.extension"));
  }

  @Test
  public void shouldResolveUnitTestFileFromAbsolutePath() {
    List<String> sources = Arrays.asList("/home/project/test/");

    Resource file = Php.newUnitTestFileFromAbsolutePath("/home/project/test/MyFileTest.php", sources);
    assertThat(file.getKey(), is("MyFileTest.php"));
    assertThat(file.getName(), is("MyFileTest.php"));
    assertTrue(file.isUnitTest());

    Resource fileUnderDir = Php.newUnitTestFileFromAbsolutePath("/home/project/test/common/MyFileTest.php", sources);
    assertThat(fileUnderDir.getKey(), is("common/MyFileTest.php"));
    assertThat(fileUnderDir.getName(), is("MyFileTest.php"));
    assertTrue(file.isUnitTest());
  }

}
