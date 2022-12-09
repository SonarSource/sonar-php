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
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.symbols.SymbolTable;

public class Cache {

  public static final String CACHE_KEY_DATA = "php.projectSymbolData.data:";
  public static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.stringTable:";
  private final CacheContext cacheContext;

  public Cache(CacheContext cacheContext) {
    this.cacheContext = cacheContext;
  }

  public void write(String key, SymbolTable symbolTable) {
    if (cacheContext.isCacheEnabled()) {
      String pluginVersion = cacheContext.pluginVersion();
      String projectKey = cacheContext.projectKey();
      SerializationInput serializationInput = new SerializationInput(projectSymbolData, pluginVersion);
      SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);
      cacheContext.getWriteCache().writeBytes(CACHE_KEY_DATA + projectKey + ":" + key, serializationData.data());
      cacheContext.getWriteCache().writeBytes(CACHE_KEY_STRING_TABLE + projectKey + ":" + key, serializationData.stringTable());
    }
  }

  @CheckForNull
  public SymbolTable read(String key) {
    if (cacheContext.isCacheEnabled()) {
      String pluginVersion = cacheContext.pluginVersion();
      String projectKey = cacheContext.projectKey();
      byte[] data = cacheContext.getReadCache().readBytes(CACHE_KEY_DATA + projectKey +  ":" + key);
      byte[] stringTable = cacheContext.getReadCache().readBytes(CACHE_KEY_STRING_TABLE + projectKey + ":" + key);
      if (data != null && stringTable != null) {
        return SymbolTableDeserializer.fromBinary(new DeserializationInput(data, stringTable, pluginVersion));
      }
    }
    return null;
  }
}
