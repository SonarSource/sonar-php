package org.sonar.php.cache;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.symbols.ProjectSymbolTableTest;

import static org.mockito.ArgumentMatchers.eq;
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

    verify(writeCache).write(CACHE_KEY_DATA, ProjectSymbolDataSerializer.toBinary(data).data());
    verify(writeCache).write(CACHE_KEY_STRING_TABLE, ProjectSymbolDataSerializer.toBinary(data).stringTable());
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
