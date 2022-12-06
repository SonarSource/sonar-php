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
import org.sonar.php.symbols.ProjectSymbolData;

public class CacheContextImpl implements CacheContext {

  public static final Version MINIMUM_RUNTIME_VERSION = Version.create(9, 7);
  private final boolean isCacheEnabled;
  private final PhpWriteCache writeCache;
  private final PhpReadCache readCache;
  private final String pluginVersion;
  private final String projectKey;

  CacheContextImpl(boolean isCacheEnabled, PhpWriteCache writeCache, PhpReadCache readCache, String pluginVersion, String projectKey) {
    this.isCacheEnabled = isCacheEnabled;
    this.writeCache = writeCache;
    this.readCache = readCache;
    this.pluginVersion = pluginVersion;
    this.projectKey = projectKey;
  }

  public static CacheContextImpl of(SensorContext context) {
    String pluginVersion = getImplementationVersion(ProjectSymbolData.class);
    String projectKey = context.project().key();
    if (!context.runtime().getProduct().equals(SonarProduct.SONARLINT)
      && context.runtime().getApiVersion().isGreaterThanOrEqual(MINIMUM_RUNTIME_VERSION)) {
      return new CacheContextImpl(context.isCacheEnabled(),
        new PhpWriteCacheImpl(context.nextCache()),
        new PhpReadCacheImpl(context.previousCache()),
        pluginVersion,
        projectKey);
    }
    return new CacheContextImpl(false, null, null, pluginVersion, projectKey);
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

  @Override
  public String pluginVersion() {
    return pluginVersion;
  }

  @Override
  public String projectKey() {
    return projectKey;
  }

  public static String getImplementationVersion(Class<?> cls) {
    String implementationVersion = cls.getPackage().getImplementationVersion();
    return implementationVersion != null ? implementationVersion : "unknown";
  }
}
