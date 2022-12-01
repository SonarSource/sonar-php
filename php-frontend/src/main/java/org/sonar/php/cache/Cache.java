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

import org.sonar.php.symbols.ProjectSymbolData;

public class Cache {

  public static final String CACHE_KEY_DATA = "php.projectSymbolData.classSymbolsByQualifiedName.data";
  public static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.classSymbolsByQualifiedName.data";
  private final CacheContext cacheContext;

  public Cache(CacheContext cacheContext) {
    this.cacheContext = cacheContext;
  }

  public void write(ProjectSymbolData projectSymbolData) {
    if (cacheContext.isCacheEnabled()) {
      ProjectSymbolDataSerializer.SerializationData serializationData = ProjectSymbolDataSerializer.toBinary(projectSymbolData);
      cacheContext.getWriteCache().write(CACHE_KEY_DATA, serializationData.data());
      cacheContext.getWriteCache().write(CACHE_KEY_STRING_TABLE, serializationData.stringTable());
    }
  }

  public ProjectSymbolData read() {
    if (cacheContext.isCacheEnabled()) {
      byte[] data = cacheContext.getReadCache().readBytes(CACHE_KEY_DATA);
      byte[] stringTable = cacheContext.getReadCache().readBytes(CACHE_KEY_STRING_TABLE);
      return ProjectSymbolDataDeserializer.fromBinary(data, stringTable);
    }
    return null;
  }
}
