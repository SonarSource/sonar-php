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

import org.junit.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.api.SonarProduct.SONARLINT;
import static org.sonar.api.SonarProduct.SONARQUBE;

public class CacheContextImplTest {

  @Test
  public void shouldCreateEnabledCacheContext() {
    SensorContext sensorContext = mock(SensorContext.class);
    SonarRuntime runtime = mock(SonarRuntime.class);
    when(runtime.getApiVersion()).thenReturn(Version.parse("9.7"));
    when(runtime.getProduct()).thenReturn(SONARQUBE);
    when(sensorContext.runtime()).thenReturn(runtime);
    when(sensorContext.isCacheEnabled()).thenReturn(true);

    CacheContextImpl context = CacheContextImpl.of(sensorContext);

    assertThat(context.isCacheEnabled()).isTrue();
    assertThat(context.getReadCache()).isInstanceOf(PhpReadCacheImpl.class);
    assertThat(context.getWriteCache()).isInstanceOf(PhpWriteCacheImpl.class);
  }

  @Test
  public void shouldCreateDisabledCacheContextForSonarLint() {
    SensorContext sensorContext = mock(SensorContext.class);
    SonarRuntime runtime = mock(SonarRuntime.class);
    when(runtime.getApiVersion()).thenReturn(Version.parse("9.7"));
    when(runtime.getProduct()).thenReturn(SONARLINT);
    when(sensorContext.runtime()).thenReturn(runtime);

    CacheContextImpl context = CacheContextImpl.of(sensorContext);

    assertThat(context.isCacheEnabled()).isFalse();
    assertThat(context.getReadCache()).isNull();
    assertThat(context.getWriteCache()).isNull();
  }

  @Test
  public void shouldCreateDisabledCacheContextForOldSonarQube() {
    SensorContext sensorContext = mock(SensorContext.class);
    SonarRuntime runtime = mock(SonarRuntime.class);
    when(runtime.getApiVersion()).thenReturn(Version.parse("9.6"));
    when(runtime.getProduct()).thenReturn(SONARQUBE);
    when(sensorContext.runtime()).thenReturn(runtime);

    CacheContextImpl context = CacheContextImpl.of(sensorContext);

    assertThat(context.isCacheEnabled()).isFalse();
    assertThat(context.getReadCache()).isNull();
    assertThat(context.getWriteCache()).isNull();
  }
}
