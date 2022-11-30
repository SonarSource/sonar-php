package org.sonar.plugins.php.cache;

import org.junit.Test;
import org.sonar.php.symbols.ProjectSymbolData;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectSymbolDataSerializerTest {

  @Test
  public void shouldSerializeData() {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    // todo create PSD
    byte[] bytes = ProjectSymbolDataSerializer.toBinary(projectSymbolData);
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(bytes);
    assertThat(actual).isEqualTo(projectSymbolData);
  }
}
