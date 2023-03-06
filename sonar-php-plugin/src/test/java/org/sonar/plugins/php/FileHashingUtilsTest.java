/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.PhpTestUtils.inputFile;

public class FileHashingUtilsTest {

  @Test
  public void hashing() throws IOException, NoSuchAlgorithmException {
    InputFile file1 = inputFile("hash/main.php", InputFile.Type.MAIN, InputFile.Status.SAME);
    InputFile file2 = inputFile("hash/modified.php", InputFile.Type.MAIN, InputFile.Status.CHANGED);
    assertThat(MessageDigest.isEqual(FileHashingUtils.inputFileContentHash(file1), FileHashingUtils.inputFileContentHash(file1))).isTrue();
    assertThat(MessageDigest.isEqual(FileHashingUtils.inputFileContentHash(file1), FileHashingUtils.inputFileContentHash(file2))).isFalse();
  }
}
