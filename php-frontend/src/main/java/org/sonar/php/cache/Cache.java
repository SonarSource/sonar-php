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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpWriteCache;

public class Cache {

  public static final String CACHE_KEY_DATA = "php.projectSymbolData.data:";
  public static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.stringTable:";
  private final CacheContext cacheContext;
  private final String pluginVersion;
  private final String projectKey;

  Map<InputFile, BigInteger> hashes = new HashMap<>();

  public Cache(CacheContext cacheContext) {
    this.cacheContext = cacheContext;
    this.pluginVersion = cacheContext.pluginVersion();
    this.projectKey = cacheContext.projectKey();
  }

  public void write(InputFile file, SymbolTableImpl symbolTable) {
    if (cacheContext.isCacheEnabled()) {
      SerializationInput serializationInput = new SerializationInput(symbolTable, pluginVersion);
      SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);
      PhpWriteCache writeCache = cacheContext.getWriteCache();
      writeCache.writeBytes(getDataCacheKey(file), serializationData.data());
      writeCache.writeBytes(getStringTableCacheKey(file), serializationData.stringTable());
    }
  }

  @CheckForNull
  public SymbolTableImpl read(InputFile file) {
    if (cacheContext.isCacheEnabled()) {
      byte[] data = cacheContext.getReadCache().readBytes(getDataCacheKey(file));
      byte[] stringTable = cacheContext.getReadCache().readBytes(getStringTableCacheKey(file));
      if (data != null && stringTable != null) {
        return SymbolTableDeserializer.fromBinary(new DeserializationInput(data, stringTable, pluginVersion));
      }
    }
    return null;
  }

  private String getDataCacheKey(InputFile file) {
    return CACHE_KEY_DATA + projectKey + ":" + getCacheFileName(file);
  }

  private String getStringTableCacheKey(InputFile file) {
    return CACHE_KEY_STRING_TABLE + projectKey + ":" + getCacheFileName(file);
  }

  private String getCacheFileName(InputFile file) {
    MessageDigest digest = getMessageDigest();
    digest.update(computeFileHash(file).toByteArray());
    return new BigInteger(1, digest.digest()).toString();
  }

  public BigInteger computeFileHash(InputFile file) {
    return hashes.computeIfAbsent(file, s -> {
      try(InputStream is = file.inputStream()) {
        byte[] bytes = is.readAllBytes();
        return hash(bytes);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  public static BigInteger hash(byte[] bytes) {
    return new BigInteger(1, getMessageDigest().digest(bytes));
  }

  private static MessageDigest getMessageDigest() {
    final java.security.MessageDigest digest;
    try {
      digest = java.security.MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    return digest;
  }
}
