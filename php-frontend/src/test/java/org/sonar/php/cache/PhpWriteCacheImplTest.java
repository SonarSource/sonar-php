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

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.cache.WriteCache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PhpWriteCacheImplTest {

  private static final byte[] bytes = new byte[] {1, 2, 3};

  @Test
  void shouldWriteFromWriteCache() {
    WriteCache writeCache = mock(WriteCache.class);
    PhpWriteCacheImpl cache = new PhpWriteCacheImpl(writeCache);

    cache.writeBytes("key", bytes);

    verify(writeCache).write("key", bytes);
  }

  @Test
  void shouldCallCopyFromPrevious() {
    WriteCache writeCache = mock(WriteCache.class);
    PhpWriteCacheImpl cache = new PhpWriteCacheImpl(writeCache);

    cache.copyFromPrevious("key");

    verify(writeCache).copyFromPrevious("key");
  }
}
