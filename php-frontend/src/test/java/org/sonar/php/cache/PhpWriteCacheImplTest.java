package org.sonar.php.cache;


import org.junit.Test;
import org.sonar.api.batch.sensor.cache.WriteCache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PhpWriteCacheImplTest {

  private final static byte [] bytes = new byte[] {1, 2, 3};

  @Test
  public void shouldWriteFromWriteCache() {
    WriteCache writeCache = mock(WriteCache.class);
    PhpWriteCacheImpl cache = new PhpWriteCacheImpl(writeCache);

    cache.write("key", bytes);

    verify(writeCache).write("key", bytes);
  }

  @Test
  public void shouldCallCopyFromPrevious() {
    WriteCache writeCache = mock(WriteCache.class);
    PhpWriteCacheImpl cache = new PhpWriteCacheImpl(writeCache);

    cache.copyFromPrevious("key");

    verify(writeCache).copyFromPrevious("key");
  }
}
