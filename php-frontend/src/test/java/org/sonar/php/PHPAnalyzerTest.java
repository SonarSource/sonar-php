/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php;

import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.php.filters.SuppressWarningFilter;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PHPAnalyzerTest {

  private final SensorContextTester sensorContext = SensorContextTester.create(Paths.get("."));

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @TempDir
  public File tempFolder;

  @Test
  void parsingFailureShouldRaiseAnException() {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = createAnalyzer(check);
    InputFile file = FileTestUtils.getInputFile(new File(tempFolder, "file"), "<?php if(condition): ?>");
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> analyzer.nextFile(file));
  }

  @Test
  void testAnalyze() {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = createAnalyzer(check);
    InputFile file = FileTestUtils.getInputFile(new File(tempFolder, "file"), "<?php $a = 1;");
    analyzer.nextFile(file);
    List<PhpIssue> issues = analyzer.analyze();
    assertThat(issues).hasSize(1);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().startLine()).isEqualTo(1);
    assertThat(issues.get(0).check()).isEqualTo(check);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().message()).isEqualTo(DummyCheck.MESSAGE);

    FileMeasures measures = analyzer.computeMeasures(mock(FileLinesContext.class));
    assertThat(measures.getLinesOfCodeNumber()).isEqualTo(1);
    Set<Integer> noSonarLines = analyzer.computeNoSonarLines();
    assertThat(noSonarLines).isEmpty();
    CompilationUnitTree compilationUnitTree = analyzer.currentFileTree();
    assertThat(compilationUnitTree.toString()).hasToString("<?php $a = 1;");
    SymbolTable symbolTable = analyzer.currentFileSymbolTable();
    assertThat(symbolTable.getSymbols(Symbol.Kind.VARIABLE).get(0).name()).isEqualTo("$a");
  }

  @Test
  void testAnalyseWithStackOverflow() {
    PHPCheck check = spy(PHPCheck.class);
    when(check.analyze(any(CheckContext.class))).thenThrow(StackOverflowError.class);

    PHPAnalyzer analyzer = createAnalyzer(check);
    InputFile file = FileTestUtils.getInputFile(new File(tempFolder, "file"), "<?php $a = 1;");
    analyzer.nextFile(file);

    assertThatExceptionOfType(StackOverflowError.class).isThrownBy(analyzer::analyze);
    assertThat(logTester.logs(Level.ERROR)).hasSize(1);
    assertThat(logTester.logs(Level.ERROR).get(0)).startsWith("Stack overflow");
  }

  @Test
  void testAnalyzeTestFile() {
    PHPCheck check = new DummyCheck();
    PHPCheck testCheck = new DummyCheck();
    PHPAnalyzer analyzer = createAnalyzer(Arrays.asList(check, testCheck), Collections.singletonList(testCheck));
    InputFile file = FileTestUtils.getInputFile(new File(tempFolder, "file"), "<?php $a = 1;");
    analyzer.nextFile(file);
    List<PhpIssue> issues = analyzer.analyze();
    assertThat(issues).hasSize(2);

    issues = analyzer.analyzeTest();
    assertThat(issues).hasSize(1);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().startLine()).isEqualTo(1);
    assertThat(issues.get(0).check()).isEqualTo(testCheck);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().message()).isEqualTo(DummyCheck.MESSAGE);
  }

  @Test
  void terminateCallForwardedToChecks() {
    PHPCheck check1 = spy(new DummyCheck());
    PHPCheck check2 = spy(new DummyCheck());
    PHPAnalyzer analyzer = createAnalyzer(check1, check2);
    analyzer.terminate();

    verify(check1).terminate();
    verify(check2).terminate();
  }

  @Test
  void logErrorAndContinueWhenExceptionInTerminate() {
    PHPCheck check1 = spy(new DummyCheck());
    doThrow(new RuntimeException("myError")).when(check1).terminate();
    PHPCheck check2 = spy(new DummyCheck());
    PHPAnalyzer analyzer = createAnalyzer(check1, check2);
    analyzer.terminate();

    verify(check1).terminate();
    verify(check2).terminate();
    assertThat(logTester.logs(Level.WARN)).contains("An error occurred while trying to terminate checks:");
  }

  @Test
  void testSuppressWarningFilterForMainAndTestFile() {
    PHPCheck check = new DummyCheck();
    PHPCheck testCheck = new DummyCheck();
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    PHPAnalyzer analyzer = createAnalyzer(List.of(check), List.of(testCheck), suppressWarningFilter, true);
    InputFile file = new TestInputFileBuilder("projectKey", "file.php")
      .setContents("<?php\n//@SuppressWarnings(\"S11\")\n$a = 1;")
      .build();
    String fileUri = file.uri().toString();

    analyzer.nextFile(file);

    List<PhpIssue> issues = analyzer.analyze();
    assertThat(issues).hasSize(1);
    assertThat(suppressWarningFilter.accept(fileUri, "S11", 3)).isFalse();
    assertThat(suppressWarningFilter.accept(fileUri, "S11", 2)).isTrue();

    suppressWarningFilter.reset();
    issues = analyzer.analyzeTest();
    assertThat(issues).hasSize(1);
    assertThat(suppressWarningFilter.accept(fileUri, "S11", 3)).isFalse();
    assertThat(suppressWarningFilter.accept(fileUri, "S11", 2)).isTrue();
  }

  @Test
  void frameworkVisitorShouldBeTriggered() {
    PHPCheck check = new DummyCheck();
    PHPCheck testCheck = new DummyCheck();
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    PHPAnalyzer analyzer = createAnalyzer(List.of(check), List.of(testCheck), suppressWarningFilter, true);
    InputFile file = new TestInputFileBuilder("projectKey", "file.php")
      .setContents("<?php\nuse Drupal\\something;\n$a = 1;")
      .build();

    analyzer.nextFile(file);
    assertThat(analyzer.currentFileSymbolTable().getFramework()).isEqualTo(SymbolTable.Framework.DRUPAL);
  }

  @Test
  void frameworkVisitorShouldNotBeTriggered() {
    PHPCheck check = new DummyCheck();
    PHPCheck testCheck = new DummyCheck();
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    PHPAnalyzer analyzer = createAnalyzer(List.of(check), List.of(testCheck), suppressWarningFilter, false);
    InputFile file = new TestInputFileBuilder("projectKey", "file.php")
      .setContents("<?php\nuse Drupal\\something;\n$a = 1;")
      .build();

    analyzer.nextFile(file);
    assertThat(analyzer.currentFileSymbolTable().getFramework()).isEqualTo(SymbolTable.Framework.EMPTY);
  }

  private PHPAnalyzer createAnalyzer(PHPCheck... checks) {
    return createAnalyzer(Arrays.asList(checks), Collections.emptyList());
  }

  private PHPAnalyzer createAnalyzer(List<PHPCheck> checks, List<PHPCheck> testFileChecks) {
    return createAnalyzer(checks, testFileChecks, new SuppressWarningFilter(), true);
  }

  private PHPAnalyzer createAnalyzer(List<PHPCheck> checks, List<PHPCheck> testFileChecks, SuppressWarningFilter suppressWarningFilter, boolean frameworkDetectionEnabled) {
    return new PHPAnalyzer(checks, testFileChecks, tempFolder, new ProjectSymbolData(),
      new DurationStatistics(sensorContext.config()), null, suppressWarningFilter, frameworkDetectionEnabled);
  }
}
