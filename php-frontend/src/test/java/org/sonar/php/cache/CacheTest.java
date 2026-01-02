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

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.tree.symbols.SymbolReturnType;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpReadCache;
import org.sonar.plugins.php.api.cache.PhpWriteCache;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CacheTest {

  private static final InputFile DEFAULT_INPUT_FILE = inputFile("default");
  private static final String CACHE_KEY_DATA = "php.projectSymbolData.data:" + DEFAULT_INPUT_FILE.key();
  private static final String CACHE_KEY_STRING_TABLE = "php.projectSymbolData.stringTable:" + DEFAULT_INPUT_FILE.key();
  private static final String CACHE_KEY_HASH = "php.contentHashes:" + DEFAULT_INPUT_FILE.key();
  private static final String PLUGIN_VERSION = "1.2.3";

  private final PhpWriteCache writeCache = mock(PhpWriteCache.class);
  private final PhpReadCache readCache = mock(PhpReadCache.class);

  @Test
  void shouldWriteToCacheOnlyIfItsEnabled() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    cache.writeFileSymbolTable(DEFAULT_INPUT_FILE, data);

    SerializationResult binary = SymbolTableSerializer.toBinary(new SymbolTableSerializationInput(data, PLUGIN_VERSION));

    verify(writeCache).writeBytes(CACHE_KEY_DATA, binary.data());
    verify(writeCache).writeBytes(CACHE_KEY_STRING_TABLE, binary.stringTable());
  }

  @Test
  void shouldNotWriteToCacheIfItsDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = emptySymbolTable();

    cache.writeFileSymbolTable(DEFAULT_INPUT_FILE, data);

    verifyNoInteractions(writeCache);
  }

  @Test
  void shouldReadFromCache() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    warmupReadCache(data);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);
    assertThat(actual).usingRecursiveComparison().isEqualTo(data);
  }

  @Test
  void shouldReturnNullWhenDataCacheEntryDoesNotExist() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);

    SymbolTableImpl data = exampleSymbolTable();
    SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA)).thenReturn(null);
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE)).thenReturn(serializationData.stringTable());

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  void shouldReturnNullWhenStringTableCacheEntryDoesNotExist() {
    CacheContext context = new CacheContextImpl(true, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);

    SymbolTableImpl data = exampleSymbolTable();
    SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA)).thenReturn(serializationData.data());
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE)).thenReturn(null);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  void shouldReturnNullWhenCacheDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    SymbolTableImpl data = exampleSymbolTable();
    warmupReadCache(data);

    SymbolTableImpl actual = cache.read(DEFAULT_INPUT_FILE);

    assertThat(actual).isNull();
  }

  @Test
  void readFileContentHashWhenCacheIsEnabled() {
    CacheContext context = new CacheContextImpl(true, null, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    byte[] hash = "hash".getBytes();

    when(readCache.readBytes(CACHE_KEY_HASH)).thenReturn(hash);

    assertThat(cache.readFileContentHash(DEFAULT_INPUT_FILE)).isEqualTo(hash);
  }

  @Test
  void readFileContentHashWhenCacheIsDisabled() {
    CacheContext context = new CacheContextImpl(false, null, readCache, PLUGIN_VERSION);
    Cache cache = new Cache(context);

    assertThat(cache.readFileContentHash(DEFAULT_INPUT_FILE)).isNull();
  }

  @Test
  void writeFileContentHashWhenCacheIsEnabled() {
    CacheContext context = new CacheContextImpl(true, writeCache, null, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    byte[] hash = DEFAULT_INPUT_FILE.md5Hash().getBytes(StandardCharsets.UTF_8);
    cache.writeFileContentHash(DEFAULT_INPUT_FILE, hash);

    verify(writeCache).writeBytes(CACHE_KEY_HASH, hash);
  }

  @Test
  void writeFileContentHashWhenCacheIsDisabled() {
    CacheContext context = new CacheContextImpl(false, writeCache, null, PLUGIN_VERSION);
    Cache cache = new Cache(context);
    cache.writeFileContentHash(DEFAULT_INPUT_FILE, new byte[] {});

    verifyNoInteractions(writeCache);
  }

  void warmupReadCache(SymbolTableImpl data) {
    SymbolTableSerializationInput serializationInput = new SymbolTableSerializationInput(data, PLUGIN_VERSION);
    SerializationResult serializationData = SymbolTableSerializer.toBinary(serializationInput);

    when(readCache.readBytes(CACHE_KEY_DATA)).thenReturn(serializationData.data());
    when(readCache.readBytes(CACHE_KEY_STRING_TABLE)).thenReturn(serializationData.stringTable());
  }

  private static SymbolTableImpl exampleSymbolTable() {
    return SymbolTableImpl.create(List.of(), List.of(new FunctionSymbolData(
      new LocationInFileImpl("abc.php", 1, 1, 1, 10),
      QualifiedName.qualifiedName("funcName"),
      List.of(),
      new FunctionSymbolData.FunctionSymbolProperties(false, false),
      new SymbolReturnType(false, false))));
  }

  private SymbolTableImpl emptySymbolTable() {
    return SymbolTableImpl.create(List.of(), List.of());
  }

  private static InputFile inputFile(String content) {
    return new TestInputFileBuilder("projectKey", "symbols/symbolTable.php")
      .setContents(content)
      .build();
  }
}
