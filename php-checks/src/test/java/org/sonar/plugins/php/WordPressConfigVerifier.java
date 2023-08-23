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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.tests.PHPCheckVerifier;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WordPressConfigVerifier {

  @TempDir
  private final File folder;

  public WordPressConfigVerifier(File folder) {
    this.folder = folder;
  }

  public void verify(PHPCheck check, String relativePath) throws IOException {
    PHPCheckVerifier.verify(checkFile(relativePath), check);
  }

  public void verifyNoIssue(PHPCheck check, String relativePath) throws IOException {
    PHPCheckVerifier.verifyNoIssue(checkFile(relativePath), check);
  }

  public void verifyAbsence(PHPCheck check, String message) throws IOException {
    File emptyFile = configFile();
    Files.write(emptyFile.toPath(), "<?php\n".getBytes(Charset.defaultCharset()));
    PHPCheckTest.check(check, TestUtils.getFile(emptyFile), Collections.singletonList(new FileIssue(check, message)));

    File noConfigFile = new File(folder, "non-wp-config.php");
    Files.write(noConfigFile.toPath(), "<?php\n".getBytes(Charset.defaultCharset()));
    PHPCheckVerifier.verifyNoIssue(noConfigFile, check);
  }

  private File checkFile(String relativePath) throws IOException {
    File originalFile = new File("src/test/resources/checks/" + relativePath);
    String content = new String(Files.readAllBytes(originalFile.toPath()), UTF_8);
    File checkFile = configFile();
    Files.write(checkFile.toPath(), content.getBytes(Charset.defaultCharset()));
    return checkFile;
  }

  private File configFile() throws IOException {
    return new File(folder, "wp-config.php");
  }
}
