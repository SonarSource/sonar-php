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
package org.sonar.php.compat;

import java.io.IOException;
import java.net.URI;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class PhpFileImpl implements PhpFile {

  private final InputFile inputFile;

  private PhpFileImpl(InputFile inputFile) {
    this.inputFile = inputFile;
  }

  public static PhpFile create(InputFile inputFile) {
    return new PhpFileImpl(inputFile);
  }

  static class InputFileIOException extends RuntimeException {
    InputFileIOException(Throwable cause) {
      super(cause);
    }
  }

  @Override
  public String contents() {
    try {
      return inputFile.contents();
    } catch (IOException e) {
      throw new InputFileIOException(e);
    }
  }

  @Override
  public String filename() {
    return inputFile.filename();
  }

  @Override
  public URI uri() {
    return inputFile.uri();
  }

  @Override
  public String toString() {
    return inputFile.toString();
  }

  @Override
  public String key() {
    return inputFile.key();
  }
}
