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
package org.sonar.plugins.php.api.cache;

import org.sonar.api.Beta;

@Beta
public interface PhpWriteCache {
  /**
   * Save a new entry in the cache.
   * @throws {@code IllegalArgumentException} if the cache already contains the key.
   */
  void writeBytes(String key, byte[] data);

  /**
   * Copy a cached entry from the previous cache to the new one.
   * @throws {@code IllegalArgumentException} if the previous cache doesn't contain given key or if this cache already contains the key.
   */
  void copyFromPrevious(String key);
}
