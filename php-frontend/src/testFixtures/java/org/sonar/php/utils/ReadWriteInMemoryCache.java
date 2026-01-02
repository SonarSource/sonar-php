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
package org.sonar.php.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.batch.sensor.cache.ReadCache;
import org.sonar.api.batch.sensor.cache.WriteCache;

public class ReadWriteInMemoryCache implements ReadCache, WriteCache {

  private Map<String, byte[]> storage = new HashMap<>();
  private final List<String> readKeys = new ArrayList<>();
  private final List<String> writeKeys = new ArrayList<>();

  @Override
  public InputStream read(String key) {
    readKeys.add(key);
    byte[] bytes = storage.get(key);
    return new ByteArrayInputStream(bytes);
  }

  @Override
  public boolean contains(String key) {
    return storage.containsKey(key);
  }

  @Override
  public void write(String key, InputStream data) {
    writeKeys.add(key);
    try {
      storage.put(key, data.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(String key, byte[] data) {
    writeKeys.add(key);
    storage.put(key, data);
  }

  @Override
  public void copyFromPrevious(String key) {

  }

  public ReadWriteInMemoryCache copy() {
    ReadWriteInMemoryCache newCache = new ReadWriteInMemoryCache();
    newCache.storage = storage;
    return newCache;
  }

  public List<String> readKeys() {
    return readKeys;
  }

  public List<String> writeKeys() {
    return writeKeys;
  }

  @Override
  public String toString() {
    return "ReadWriteInMemoryCache{" +
      "readKeys=" + readKeys +
      ", writeKeys=" + writeKeys +
      ", storage=" + storage +
      '}';
  }
}
