/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PhpTestFile implements PhpFile {

  private final Path relativePath;

  private final String contents;

  public PhpTestFile(File file) {
    try {
      relativePath = file.toPath();
      contents = new String(Files.readAllBytes(file.toPath()), UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid file: " + file, e);
    }
  }

  @Override
  public String contents() {
    return contents;
  }

  @Override
  public String filename() {
    return relativePath.getFileName().toString();
  }

  @Override
  public URI uri() {
    return relativePath.toUri();
  }

  @Override
  public String toString() {
    return relativePath.toString();
  }

  @Override
  public String key() {
    return "testFileModule:" + filename();
  }
}
