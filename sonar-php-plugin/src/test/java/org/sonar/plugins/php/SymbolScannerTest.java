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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.sonar.DurationStatistics;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
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
    SymbolScanner symbolScanner = createScanner();

    List<InputFile> inputFiles = exampleFiles("Mail.php", "cpd.php", "Math2.php", "Math3.php", "PHPSquidSensor.php",
      "cross-file/A.php", "cross-file/B.php"
    );

    symbolScanner.execute(inputFiles);

    ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();
    assertThat(projectSymbolData.classSymbolsByQualifiedName()).isNotEmpty();

    SerializationResult binary = ProjectSymbolDataSerializer.toBinary(new SerializationInput(projectSymbolData, "1.2.3"));
    ProjectSymbolData actual = ProjectSymbolDataDeserializer.fromBinary(new DeserializationInput(binary.data(), binary.stringTable(), "1.2.3"));

    assertThat(actual).isEqualToComparingFieldByFieldRecursively(projectSymbolData);
  }

  private static SymbolScanner createScanner() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(9, 7), SonarQubeSide.SCANNER, SonarEdition.DEVELOPER);
    SensorContextTester context = SensorContextTester.create(PhpTestUtils.getModuleBaseDir())
      .setRuntime(runtime);
    context.setCacheEnabled(true);
    ReadWriteInMemoryCache cacheMock = new ReadWriteInMemoryCache();
    context.setNextCache(cacheMock);
    context.setPreviousCache(cacheMock);

    DurationStatistics statistics = new DurationStatistics(context.config());
    CacheContext cacheContext = CacheContextImpl.of(context);
    Cache cache = new Cache(cacheContext);
    SymbolScanner symbolScanner = new SymbolScanner(context, statistics, cache);
    return symbolScanner;
  }

  private static List<InputFile> exampleFiles(String... fileNames) {
    List<InputFile> inputFiles = new ArrayList<>();

    for (String fileName : fileNames) {
      DefaultInputFile inputFile = file(fileName);
      inputFiles.add(inputFile);
    }
    return inputFiles;
  }

  private static DefaultInputFile file(String name) {
    DefaultInputFile inputFile = TestInputFileBuilder.create(PhpTestUtils.getModuleBaseDir().getPath(), name)
      .setLanguage("php")
      .setType(InputFile.Type.MAIN)
      .initMetadata("<?php ")
      .setCharset(StandardCharsets.UTF_8)
      .build();
    return inputFile;
  }
}
