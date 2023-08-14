/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.api.cache.CacheContext;

import static org.assertj.core.api.Assertions.assertThat;

class CacheContextImplTest {

  private final SensorContextTester sensorContext = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

  @Test
  void shouldCreateEnabledCacheContext() {
    sensorContext.setCacheEnabled(true);

    CacheContext cacheContext = CacheContextImpl.of(sensorContext);

    assertThat(cacheContext.isCacheEnabled()).isTrue();
    assertThat(cacheContext.getReadCache()).isInstanceOf(PhpReadCacheImpl.class);
    assertThat(cacheContext.getWriteCache()).isInstanceOf(PhpWriteCacheImpl.class);
    assertThat(cacheContext.pluginVersion()).isEqualTo("unknown");
  }

  @Test
  void shouldCreateDisabledCacheContextForSonarLint() {
    sensorContext.setRuntime(SonarRuntimeImpl.forSonarLint(Version.create(1, 2)));
    CacheContext cacheContext = CacheContextImpl.of(sensorContext);

    assertThat(cacheContext.isCacheEnabled()).isFalse();
    assertThat(cacheContext.getReadCache()).isNull();
    assertThat(cacheContext.getWriteCache()).isNull();
    assertThat(cacheContext.pluginVersion()).isEqualTo("unknown");
  }

  @Test
  void shouldCreateDisabledCacheContextForOldSonarQube() {
    sensorContext.setRuntime(SonarRuntimeImpl.forSonarQube(
      Version.parse("9.6"),
      SonarQubeSide.SCANNER,
      SonarEdition.DEVELOPER));

    CacheContext cacheContext = CacheContextImpl.of(sensorContext);

    assertThat(cacheContext.isCacheEnabled()).isFalse();
    assertThat(cacheContext.getReadCache()).isNull();
    assertThat(cacheContext.getWriteCache()).isNull();
    assertThat(cacheContext.pluginVersion()).isEqualTo("unknown");
  }

  @Test
  void shouldCreateDisabledCacheContextForEnabledSonarModules() {
    sensorContext.setSettings(new MapSettings().setProperty("sonar.modules", "module1,module2"));
    sensorContext.setCacheEnabled(true);

    CacheContext cacheContext = CacheContextImpl.of(sensorContext);

    assertThat(cacheContext.isCacheEnabled()).isFalse();
    assertThat(cacheContext.getReadCache()).isNull();
    assertThat(cacheContext.getWriteCache()).isNull();
    assertThat(cacheContext.pluginVersion()).isEqualTo("unknown");
  }
}
