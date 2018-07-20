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
package org.sonar.plugins.php.api.visitors;

import java.nio.file.Path;

/**
 * Class representing file being analysed by SonarPHP plugin.
 */
public interface PhpFile {

  /**
   * @return path relative to project directory
   * @deprecated since 2.14. Use {@link PhpFile#filename()} or {@link PhpFile#toString()} (returning a string to identify this file suitable for logs).
   */
  @Deprecated
  Path relativePath();

  String contents();

  /**
   * @return Filename for this file (inclusing extension). For example: MyFile.php.
   */
  String filename();
}
