/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.utils.ReadWriteInMemoryCache;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.sonar.plugins.php.PhpTestUtils.inputFile;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

class SymbolScannerTest {

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  public static final QualifiedName CLASS_NAME = qualifiedName("app\\test\\controller");
  private static ReadWriteInMemoryCache previousCache;
  private static ReadWriteInMemoryCache nextCache;
  private SensorContextTester context;
  private DurationStatistics statistics;

  @BeforeEach
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
  void shouldCreateProjectSymbolDataWhenCacheIsDisabled() throws IOException {
    SymbolScanner symbolScanner = createScannerCacheDisabled();
    InputFile baseFile = inputFile("incremental/baseFile.php", InputFile.Type.MAIN, InputFile.Status.ADDED);

    symbolScanner.execute(List.of(baseFile));

    ProjectSymbolData projectSymbolData = symbolScanner.getProjectSymbolData();
    ClassSymbolData classSymbolData = projectSymbolData.classSymbolData(CLASS_NAME).orElse(null);
    assertThat(classSymbolData.methods()).hasSize(2);
  }

  @Test
  void shouldCreateProjectSymbolDataFromCache() throws IOException, NoSuchAlgorithmException {
    buildBaseProjectSymbolDataAndCache();

    previousCache = nextCache.copy();
    context.setPreviousCache(previousCache);
    context.setCanSkipUnchangedFiles(true);
    SymbolScanner symbolScanner = createScanner();
    InputFile inputFile = inputFile("incremental/baseFile.php", InputFile.Type.MAIN, InputFile.Status.SAME);
    symbolScanner.execute(List.of(inputFile));

    ProjectSymbolData newSymbolTable = symbolScanner.getProjectSymbolData();

    ClassSymbolData newClassSymbol = newSymbolTable.classSymbolData(CLASS_NAME).orElse(null);
    assertThat(newClassSymbol.methods()).hasSize(2);

    assertThat(previousCache.readKeys()).containsExactly("php.contentHashes:moduleKey:incremental/baseFile.php",
      "php.projectSymbolData.data:moduleKey:incremental/baseFile.php",
      "php.projectSymbolData.stringTable:moduleKey:incremental/baseFile.php");
  }

  @Test
  void hashExceptionWhenTryingToSaveHash() {
    try (MockedStatic<FileHashingUtils> FileHashingUtilsStaticMock = Mockito.mockStatic(FileHashingUtils.class)) {
      FileHashingUtilsStaticMock.when(() -> FileHashingUtils.inputFileContentHash(any())).thenThrow(new IllegalStateException("BOOM!"));
      buildBaseProjectSymbolDataAndCache();
      assertThat(logTester.logs(Level.DEBUG)).contains("Failed to compute content hash for file moduleKey:incremental/baseFile.php");
    }
  }

  @Test
  void shouldCreateProjectSymbolDataWithSymbolWhenFileIsDeleted() {
    buildBaseProjectSymbolDataAndCache();

    previousCache = nextCache.copy();
    context.setPreviousCache(previousCache);
    context.setCanSkipUnchangedFiles(true);
    SymbolScanner symbolScanner = createScanner();
    symbolScanner.execute(Collections.emptyList());

    ProjectSymbolData symbolTable = symbolScanner.getProjectSymbolData();

    assertThat(symbolTable.classSymbolData(CLASS_NAME)).isEmpty();
  }

  private ProjectSymbolData buildBaseProjectSymbolDataAndCache() {
    SymbolScanner symbolScanner = createScanner();
    InputFile baseFile = inputFile("incremental/baseFile.php", InputFile.Type.MAIN, InputFile.Status.ADDED);
    symbolScanner.execute(List.of(baseFile));
    return symbolScanner.getProjectSymbolData();
  }

  private SymbolScanner createScanner() {
    return SymbolScanner.create(context, statistics, CacheContextImpl.of(context));
  }

  private SymbolScanner createScannerCacheDisabled() throws IOException {
    SensorContextTester sensorContext = SensorContextTester.create(PhpTestUtils.getModuleBaseDir());
    Path workDir = Files.createTempDirectory("workDir");
    sensorContext.fileSystem().setWorkDir(workDir);
    sensorContext.setCacheEnabled(false);
    previousCache = new ReadWriteInMemoryCache();
    nextCache = new ReadWriteInMemoryCache();
    sensorContext.setPreviousCache(previousCache);
    sensorContext.setNextCache(nextCache);
    statistics = new DurationStatistics(sensorContext.config());

    return SymbolScanner.create(sensorContext, statistics, CacheContextImpl.of(context));
  }
}
