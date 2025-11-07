/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.symbols;

import java.net.URI;
import java.nio.file.Paths;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class TestFile implements PhpFile {

  private final String content;
  private final String name;

  public TestFile(String content, String name) {
    this.content = content;
    this.name = name;
  }

  @Override
  public String contents() {
    return content;
  }

  @Override
  public String filename() {
    return name;
  }

  @Override
  public URI uri() {
    return Paths.get(name).toUri();
  }

  @Override
  public String key() {
    return "moduleKey:" + filename();
  }
}
