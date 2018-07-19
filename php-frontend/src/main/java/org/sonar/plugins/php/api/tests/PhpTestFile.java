/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.php.api.tests;

import java.io.File;
import java.io.IOException;
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
  public Path relativePath() {
    return relativePath;
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
  public String toString() {
    return relativePath.toString();
  }
}
