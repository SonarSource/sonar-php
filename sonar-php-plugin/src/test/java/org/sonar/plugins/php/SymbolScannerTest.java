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
package org.sonar.plugins.php;

import java.io.File;
import org.junit.Test;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.php.cache.Cache;
import org.sonar.php.cache.CacheContext;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.cache.DeserializationInput;
import org.sonar.php.cache.ProjectSymbolDataDeserializer;
import org.sonar.php.cache.ProjectSymbolDataSerializer;
import org.sonar.php.cache.SerializationInput;
import org.sonar.php.cache.SerializationResult;
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

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, "1.2.3"));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), "1.2.3"));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }
}
