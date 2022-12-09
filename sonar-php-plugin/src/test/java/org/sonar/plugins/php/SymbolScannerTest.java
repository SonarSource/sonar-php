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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.cache.ReadCache;
import org.sonar.api.batch.sensor.cache.WriteCache;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.cache.DeserializationInput;
import org.sonar.php.cache.SymbolTableDeserializer;
import org.sonar.php.cache.SymbolTableSerializer;
import org.sonar.php.cache.SerializationInput;
import org.sonar.php.cache.SerializationResult;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.PhpTestUtils.inputFile;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class SymbolScannerTest {

  private static ReadCache previousCache;
  private static WriteCache nextCache;
  private SensorContextTester context;
  private DurationStatistics statistics;

  @Before
  public void init() throws IOException {
    context = SensorContextTester.create(PhpTestUtils.getModuleBaseDir());
    Path workDir = Files.createTempDirectory("workDir");
    context.fileSystem().setWorkDir(workDir);
    context.setCacheEnabled(true);
    previousCache = new ReadWriteInMemoryCache();
    nextCache = new ReadWriteInMemoryCache();
    context.setPreviousCache(previousCache);
    context.setNextCache(nextCache);

    statistics = new DurationStatistics(context.config());
  }

  @Test
  public void shouldOverrideSymbol() {
    ProjectSymbolData cachedSymbolTable = buildBaseProjectSymbolDataAndCache();

    context.setPreviousCache((ReadCache) nextCache);
    SymbolScanner symbolScanner = createScanner();
    InputFile changedFile = inputFile("incremental/changedFile.php", InputFile.Type.MAIN, InputFile.Status.CHANGED);
    symbolScanner.execute(List.of(changedFile));
    ProjectSymbolData newSymbolTable = symbolScanner.getProjectSymbolData();

    QualifiedName className = qualifiedName("app\\test\\controller");
    ClassSymbolData cachedClassSymbol = cachedSymbolTable.classSymbolData(className).orElse(null);
    ClassSymbolData newClassSymbol = newSymbolTable.classSymbolData(className).orElse(null);

    assertThat(cachedClassSymbol).isNotNull();
    assertThat(newClassSymbol).isNotNull();

    assertThat(cachedClassSymbol.methods()).hasSize(2);
    assertThat(newClassSymbol.methods()).hasSize(3);
  }

  @Test
  public void shouldCreateNewSymbolIfCacheIsOutdated() {
    buildBaseProjectSymbolDataAndCache();

    context.setPreviousCache((ReadCache) nextCache);
    SymbolScanner symbolScanner = createScanner();
    InputFile changedFile = inputFile("incremental/baseFile.php", InputFile.Type.MAIN, InputFile.Status.CHANGED);
    symbolScanner.execute(List.of(changedFile));
    ProjectSymbolData newSymbolTable = symbolScanner.getProjectSymbolData();

    QualifiedName className = qualifiedName("app\\test\\controller");
    ClassSymbolData newClassSymbol = newSymbolTable.classSymbolData(className).orElse(null);
    assertThat(newClassSymbol).isNotNull();
    assertThat(newClassSymbol.methods()).hasSize(2);
  }

  private ProjectSymbolData buildBaseProjectSymbolDataAndCache() {
    SymbolScanner symbolScanner = createScanner();
    InputFile baseFile = inputFile("incremental/baseFile.php", InputFile.Type.MAIN, InputFile.Status.ADDED);
    symbolScanner.execute(List.of(baseFile));
    return symbolScanner.getProjectSymbolData();
  }



  private SymbolScanner createScanner() {
    return SymbolScanner.create(context, statistics);
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
    return file(name, InputFile.Status.CHANGED);
  }

  private static DefaultInputFile file(String name, InputFile.Status status) {
    DefaultInputFile inputFile = TestInputFileBuilder.create(PhpTestUtils.getModuleBaseDir().getPath(), name)
      .setLanguage("php")
      .setType(InputFile.Type.MAIN)
      .initMetadata("<?php ")
      .setStatus(status)
      .setCharset(StandardCharsets.UTF_8)
      .build();
    return inputFile;
  }
}
