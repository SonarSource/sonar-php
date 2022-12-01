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

import org.sonar.api.SonarProduct;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;

public class CacheContextImpl implements CacheContext {

  public static final Version MINIMUM_RUNTIME_VERSION = Version.create(9, 7);
  private final boolean isCacheEnabled;
  private final PhpWriteCache writeCache;
  private final PhpReadCache readCache;

  public CacheContextImpl(boolean isCacheEnabled, PhpWriteCache writeCache, PhpReadCache readCache) {
    this.isCacheEnabled = isCacheEnabled;
    this.writeCache = writeCache;
    this.readCache = readCache;
  }

  public static CacheContextImpl of(SensorContext context) {
    if (!context.runtime().getProduct().equals(SonarProduct.SONARLINT)
      && context.runtime().getApiVersion().isGreaterThanOrEqual(MINIMUM_RUNTIME_VERSION)) {
      return new CacheContextImpl(context.isCacheEnabled(),
        new PhpWriteCacheImpl(context.nextCache()),
        new PhpReadCacheImpl(context.previousCache()));
    }
    return new CacheContextImpl(false, new DummyCache(), new PhpReadCacheImpl(context.previousCache()));
  }

  @Override
  public boolean isCacheEnabled() {
    return isCacheEnabled;
  }

  @Override
  public PhpReadCache getReadCache() {
    return readCache;
  }

  @Override
  public PhpWriteCache getWriteCache() {
    return writeCache;
  }
}
