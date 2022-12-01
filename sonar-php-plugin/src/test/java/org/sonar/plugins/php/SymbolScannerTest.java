package org.sonar.plugins.php;

import java.io.File;
import org.junit.Test;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.php.cache.Cache;
import org.sonar.php.cache.CacheContext;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.cache.ProjectSymbolDataDeserializer;
import org.sonar.php.cache.ProjectSymbolDataSerializer;
import org.sonar.php.symbols.ProjectSymbolData;

import static org.assertj.core.api.Assertions.assertThat;

public class SymbolScannerTest {
  @Test
  public void shouldSerializeAndDeserializeData() {
    SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

    DurationStatistics statistics = new DurationStatistics(context.config());
    CacheContext cacheContext = CacheContextImpl.of(context);
    Cache cache = new Cache(cacheContext);
    SymbolScanner symbolScanner = new SymbolScanner(context, statistics, cache);
    ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();

    ProjectSymbolDataSerializer.SerializationData binary = ProjectSymbolDataSerializer.toBinary(projectSymbolData);
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(binary.data(), binary.stringTable());

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }
}
