/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.php.cache;

import javax.annotation.CheckForNull;

public class DummyCache implements PhpWriteCache, PhpReadCache {
  @Override
  public void write(String key, byte[] data) {
    throw new IllegalArgumentException(String.format("Same key cannot be written to multiple times (%s)", key));
  }

  @Override
  public void copyFromPrevious(String key) {
    throw new IllegalArgumentException("No cache data available");
  }

  @Override
  @CheckForNull
  public byte[] readBytes(String key) {
    return null;
  }

  @Override
  public boolean contains(String key) {
    return false;
  }
}
