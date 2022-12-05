package org.sonar.php.cache;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.sonar.api.batch.sensor.cache.ReadCache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhpReadCacheImplTest {

  @Test
  public void shouldReadBytesFromReadCache() {
    ReadCache readCache = mock(ReadCache.class);
    when(readCache.contains("key")).thenReturn(true);
    InputStream inputStream = mock(InputStream.class);
    when(readCache.read("key")).thenReturn(inputStream);
    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    phpReadCache.readBytes("key");

    verify(readCache).contains("key");
    verify(readCache).read("key");
  }

  @Test
  public void shouldReturnNullWhenIoException() throws IOException {
    ReadCache readCache = mock(ReadCache.class);
    InputStream inputStream = mock(InputStream.class);
    when(inputStream.readAllBytes()).thenThrow(new IOException());
    when(readCache.read("key")).thenReturn(inputStream);

    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    byte[] actual = phpReadCache.readBytes("key");

    assertThat(actual).isNull();
  }

  @Test
  public void shouldCheckContainsInReadCache() {
    ReadCache readCache = mock(ReadCache.class);
    PhpReadCacheImpl phpReadCache = new PhpReadCacheImpl(readCache);

    phpReadCache.contains("key");

    verify(readCache).contains("key");
  }
}
