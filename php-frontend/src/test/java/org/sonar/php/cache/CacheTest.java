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
import org.sonar.php.symbols.ProjectSymbolData;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class CacheTest {

  private static final String CACHE_KEY_DATA = "php.projectSymbolData.classSymbolsByQualifiedName.data";
  private static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.classSymbolsByQualifiedName.data";

  private PhpWriteCache writeCache = mock(PhpWriteCache.class);
  private PhpReadCache readCache = mock(PhpReadCache.class);

  @Test
  public void shouldWriteToCacheOnlyIfItsEnabled() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache);
    Cache cache = new Cache(context);
    ProjectSymbolData data = new ProjectSymbolData();

    cache.write(data);

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(data, "1.2.3"));
    verify(writeCache).write(CACHE_KEY_DATA, binary.data());
    verify(writeCache).write(CACHE_KEY_STRING_TABLE, binary.stringTable());
  }

  @Test
  public void shouldNotWriteToCacheIfItsDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache);
    Cache cache = new Cache(context);
    ProjectSymbolData data = new ProjectSymbolData();

    cache.write(data);

    verifyZeroInteractions(writeCache);
  }
}
