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

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.cache.ReadCache;
import org.sonar.plugins.php.api.cache.PhpReadCache;

public class PhpReadCacheImpl implements PhpReadCache {

  private static final Logger LOG = LoggerFactory.getLogger(PhpReadCacheImpl.class);

  private final ReadCache readCache;

  public PhpReadCacheImpl(ReadCache readCache) {
    this.readCache = readCache;
  }

  @Override
  @CheckForNull
  public byte[] readBytes(String key) {
    if (readCache.contains(key)) {
      try (var in = read(key)) {
        return in.readAllBytes();
      } catch (IOException e) {
        LOG.debug("Unable to read data for key: \"{}\"", key);
      }
    } else {
      LOG.trace("Cache miss for key '{}'", key);
    }
    return null;
  }

  @Override
  public InputStream read(String key) {
    return readCache.read(key);
  }

  @Override
  public boolean contains(String key) {
    return readCache.contains(key);
  }
}
