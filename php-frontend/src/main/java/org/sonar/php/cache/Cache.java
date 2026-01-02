/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.cache;

import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpWriteCache;

public class Cache {

  public static final String DATA_CACHE_PREFIX = "php.projectSymbolData.data:";
  public static final String STRING_TABLE_CACHE_PREFIX = "php.projectSymbolData.stringTable:";
  public static final String CONTENT_HASHES_KEY = "php.contentHashes:";
  private final CacheContext cacheContext;
  private final String pluginVersion;

  public Cache(CacheContext cacheContext) {
    this.cacheContext = cacheContext;
    this.pluginVersion = cacheContext.pluginVersion();
  }

  public void writeFileSymbolTable(InputFile file, SymbolTableImpl symbolTable) {
    if (cacheContext.isCacheEnabled()) {
      SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(symbolTable, pluginVersion);
      SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

      PhpWriteCache writeCache = cacheContext.getWriteCache();
      writeCache.writeBytes(cacheKey(DATA_CACHE_PREFIX, file.key()), serializationData.data());
      writeCache.writeBytes(cacheKey(STRING_TABLE_CACHE_PREFIX, file.key()), serializationData.stringTable());
    }
  }

  public void writeFileContentHash(InputFile file, byte[] hash) {
    if (cacheContext.isCacheEnabled()) {
      String cacheKey = cacheKey(CONTENT_HASHES_KEY, file.key());
      cacheContext.getWriteCache().writeBytes(cacheKey, hash);
    }
  }

  @CheckForNull
  public SymbolTableImpl read(InputFile file) {
    if (cacheContext.isCacheEnabled()) {
      byte[] data = cacheContext.getReadCache().readBytes(cacheKey(DATA_CACHE_PREFIX, file.key()));
      byte[] stringTable = cacheContext.getReadCache().readBytes(cacheKey(STRING_TABLE_CACHE_PREFIX, file.key()));
      if (data != null && stringTable != null) {
        return SymbolTableDeserializer.fromBinary(new SymbolTableDeserializationInput(data, stringTable, pluginVersion));
      }
    }
    return null;
  }

  @CheckForNull
  public byte[] readFileContentHash(InputFile file) {
    if (cacheContext.isCacheEnabled()) {
      String cacheKey = cacheKey(CONTENT_HASHES_KEY, file.key());
      return cacheContext.getReadCache().readBytes(cacheKey);
    }
    return null;
  }

  private static String cacheKey(String prefix, String file) {
    return prefix + file.replace('\\', '/');
  }

}
