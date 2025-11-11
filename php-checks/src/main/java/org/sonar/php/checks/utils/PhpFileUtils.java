/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public class PhpFileUtils {
  private PhpFileUtils() {
  }

  public static boolean isImportmapPhp(String stringFilePath) {
    Path filePath = Path.of(stringFilePath);
    return Files.isRegularFile(filePath) && "importmap.php".equals(filePath.getFileName().toString());
  }
}
