/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.php.compat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

/**
 * A compatibility wrapper for InputFile. See class hierarchy.
 */
class CompatibleInputFile implements PhpFile {

  private final InputFile wrapped;

  CompatibleInputFile(InputFile wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public Path relativePath() {
    return Paths.get(wrapped.relativePath());
  }

  @Override
  public File file() {
    return wrapped.file();
  }

  Path path() {
    return wrapped.path();
  }

  static class InputFileIOException extends RuntimeException {
    InputFileIOException(Throwable cause) {
      super(cause);
    }
  }

  @Override
  public String contents() {
    try {
      return wrapped.contents();
    } catch (IOException e) {
      throw new InputFileIOException(e);
    }
  }

  Charset charset() {
    return wrapped.charset();
  }
}
