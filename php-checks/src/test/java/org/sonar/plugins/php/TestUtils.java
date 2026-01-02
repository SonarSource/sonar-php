/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.sonar.php.utils.PhpTestFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestUtils {

  private TestUtils() {
  }

  public static PhpFile getCheckFile(String filename) {
    return getFile(new File("src/test/resources/checks/" + filename));
  }

  public static PhpFile getFile(File file, String contents) {
    try {
      Files.write(file.toPath(), contents.getBytes(UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write test file: " + file.getAbsolutePath());
    }
    return getFile(file);
  }

  public static PhpFile getFile(File file) {
    return new PhpTestFile(file);
  }

}
