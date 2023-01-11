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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpWriteCache;

public class Cache {

  public static final String DATA_CACHE_PREFIX = "php.projectSymbolData.data:";
  public static final String STRING_TABLE_CACHE_PREFIX = "php.projectSymbolData.stringTable:";
  private final CacheContext cacheContext;
  private final String pluginVersion;

  public Cache(CacheContext cacheContext) {
    this.cacheContext = cacheContext;
    this.pluginVersion = cacheContext.pluginVersion();
  }

  public void write(InputFile file, SymbolTableImpl symbolTable) {
    if (cacheContext.isCacheEnabled()) {
      String hash = HashProvider.hash(file);
      if (hash != null) {
        SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(symbolTable, pluginVersion);
        SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

        PhpWriteCache writeCache = cacheContext.getWriteCache();
        writeCache.writeBytes(cacheKey(DATA_CACHE_PREFIX, file, hash), serializationData.data());
        writeCache.writeBytes(cacheKey(STRING_TABLE_CACHE_PREFIX, file, hash), serializationData.stringTable());
      }
    }
  }

  @CheckForNull
  public SymbolTableImpl read(InputFile file) {
    if (cacheContext.isCacheEnabled()) {
      String hash = HashProvider.hash(file);
      if (hash != null) {
        byte[] data = cacheContext.getReadCache().readBytes(cacheKey(DATA_CACHE_PREFIX, file, hash));
        byte[] stringTable = cacheContext.getReadCache().readBytes(cacheKey(STRING_TABLE_CACHE_PREFIX, file, hash));
        if (data != null && stringTable != null) {
          return SymbolTableDeserializer.fromBinary(new SymbolTableDeserializationInput(data, stringTable, pluginVersion));
        }
      }
    }
    return null;
  }

  private static String cacheKey(String prefix, InputFile file, String suffix) {
    return prefix + file.key().replace('\\', '/') + ":" + suffix;
  }
}
