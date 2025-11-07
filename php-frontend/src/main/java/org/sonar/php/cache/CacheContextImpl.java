/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpReadCache;
import org.sonar.plugins.php.api.cache.PhpWriteCache;

public class CacheContextImpl implements CacheContext {

  private static final Logger LOG = LoggerFactory.getLogger(CacheContextImpl.class);

  public static final Version MINIMUM_RUNTIME_VERSION = Version.create(9, 7);
  private final boolean isCacheEnabled;
  private final PhpWriteCache writeCache;
  private final PhpReadCache readCache;
  private final String pluginVersion;

  public CacheContextImpl(boolean isCacheEnabled, @Nullable PhpWriteCache writeCache, @Nullable PhpReadCache readCache, String pluginVersion) {
    this.isCacheEnabled = isCacheEnabled;
    this.writeCache = writeCache;
    this.readCache = readCache;
    this.pluginVersion = pluginVersion;
  }

  public static CacheContextImpl of(SensorContext context) {
    String pluginVersion = getImplementationVersion(ProjectSymbolData.class);
    String sonarModules = context.config().get("sonar.modules").orElse("");
    if (StringUtils.isNotBlank(sonarModules) && context.isCacheEnabled()) {
      LOG.warn("The sonar.modules is a deprecated property and should not be used anymore, it inhibits an optimized analysis");
    }
    if (!context.runtime().getProduct().equals(SonarProduct.SONARLINT)
      && context.runtime().getApiVersion().isGreaterThanOrEqual(MINIMUM_RUNTIME_VERSION)
      && StringUtils.isBlank(sonarModules)) {
      return new CacheContextImpl(context.isCacheEnabled(),
        new PhpWriteCacheImpl(context.nextCache()),
        new PhpReadCacheImpl(context.previousCache()),
        pluginVersion);
    }
    return new CacheContextImpl(false, null, null, pluginVersion);
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

  public static String getImplementationVersion(Class<?> cls) {
    String implementationVersion = cls.getPackage().getImplementationVersion();
    return implementationVersion != null ? implementationVersion : "unknown";
  }
}
