/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.cache;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.fest.assertions.Assertions.assertThat;

public class HashProviderTest {

  @Test
  public void shouldProvideHashForInputFile() {
    File file = new File("src/test/resources/symbols/symbolTable.php");
    DefaultInputFile inputFile = new TestInputFileBuilder("", file.getPath())
      .setCharset(StandardCharsets.UTF_8)
      .build();

    String hash = HashProvider.hash(inputFile);

    assertThat(hash).isEqualTo("38491341469434520286006021855476626548128507421389423066319086850845792271303");
  }

  @Test
  public void shouldProvideHashForPhpFile() {
    File file = new File("src/test/resources/symbols/symbolTable.php");
    DefaultInputFile inputFile = new TestInputFileBuilder("", file.getPath())
      .setCharset(StandardCharsets.UTF_8)
      .build();
    PhpFile phpFile = PhpFileImpl.create(inputFile);

    String hash = HashProvider.hash(phpFile);

    assertThat(hash).isEqualTo("38491341469434520286006021855476626548128507421389423066319086850845792271303");
  }

  @Test
  public void shouldReturnsNullWhenFileDoesntExist() {
    File file = new File("do_not_exist_file.php");
    DefaultInputFile inputFile = new TestInputFileBuilder("", file.getPath())
      .setCharset(StandardCharsets.UTF_8)
      .build();

    String hash = HashProvider.hash(inputFile);

    assertThat(hash).isNull();
  }

  @Test
  public void shouldReturnsNullWhenPhpFileDoesntExist() {
    File file = new File("do_not_exist_file.php");
    DefaultInputFile inputFile = new TestInputFileBuilder("", file.getPath())
      .setCharset(StandardCharsets.UTF_8)
      .build();

    String hash = HashProvider.hash(PhpFileImpl.create(inputFile));

    assertThat(hash).isNull();
  }

  @Test
  public void shouldReturnHashForEmptyFile() {
    File file = new File("empty.php");
    DefaultInputFile inputFile = new TestInputFileBuilder("", file.getPath())
      .setCharset(StandardCharsets.UTF_8)
      .setContents("")
      .build();

    String hash = HashProvider.hash(inputFile);

    assertThat(hash).isEqualTo("-12804752987762098394035772686106585063470084017442529046078187006797464553387");
  }
}
