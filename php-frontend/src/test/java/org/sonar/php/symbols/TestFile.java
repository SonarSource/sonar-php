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
