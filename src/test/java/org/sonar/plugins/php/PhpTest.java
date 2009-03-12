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
  public void shouldGetParentReturnsRightValues(){
    Resource root = Php.newDirectory("root");
    assertThat(new Php().getParent(root), nullValue());

    Resource fileUnderRoot = Php.newDirectory("file.php");
    assertThat(new Php().getParent(fileUnderRoot), nullValue());

    Resource fileUnderADirectory = Php.newFile("src/file.php");
//    assertThat(fileUnderADirectory.getKey(), is("file.php"));
    assertThat(new Php().getParent(fileUnderADirectory), new IsPhpDirectory("src"));

    Resource fileUnderADirectory2 = Php.newFile("src", "file.php");
    assertThat(new Php().getParent(fileUnderADirectory2), new IsPhpDirectory("src"));

    Resource fileUnderDirectories = Php.newFile("src/common/file.php");
    assertThat(new Php().getParent(fileUnderDirectories), new IsPhpDirectory("src/common"));
  }

  @Test
  public void shouldResolveFileFromAbsolutePath() {
    List<String> sources = Arrays.asList("/usr/local/sources/", "/home/project/src/");

    Resource phpFile = Php.newFileFromAbsolutePath("/home/project/src/common/MyFile.php", sources);
    assertThat(phpFile.getKey(), is("common/MyFile.php"));
    assertThat(phpFile.getName(), is("MyFile.php"));
    assertThat(new Php().getParent(phpFile), new IsPhpDirectory("common"));
  }
  
  @Test
  public void shouldCheckValidPhpExtensions(){
    assertTrue(Php.containsValidSuffixes("goodExtension.php"));
    assertTrue(Php.containsValidSuffixes("goodExtension.php5"));
    assertFalse(Php.containsValidSuffixes("wrong.extension"));
  }

}
