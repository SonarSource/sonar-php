/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php;

import com.sonar.sslr.api.RecognitionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
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

public class PHPAnalyzerTest {

  @Rule
  public LogTester logTester = new LogTester();

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test(expected = RecognitionException.class)
  public void parsing_failure_should_raise_an_exception() throws IOException {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = createAnalyzer(check);
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php if(condition): ?>");
    analyzer.nextFile(file);
  }

  @Test
  public void test_analyze() throws Exception {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = createAnalyzer(check);
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php $a = 1;");
    analyzer.nextFile(file);
    List<PhpIssue> issues = analyzer.analyze();
    assertThat(issues).hasSize(1);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().startLine()).isEqualTo(1);
    assertThat(issues.get(0).check()).isEqualTo(check);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().message()).isEqualTo(DummyCheck.MESSAGE);

    FileMeasures measures = analyzer.computeMeasures(mock(FileLinesContext.class));
    assertThat(measures.getLinesOfCodeNumber()).isEqualTo(1);
  }

  @Test
  public void test_analyse_with_stack_overflow() throws Exception {
    PHPCheck check = spy(PHPCheck.class);
    when(check.analyze(any(CheckContext.class))).thenThrow(StackOverflowError.class);

    PHPAnalyzer analyzer = createAnalyzer(check);
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php $a = 1;");
    analyzer.nextFile(file);

    assertThatExceptionOfType(StackOverflowError.class).isThrownBy(analyzer::analyze);
    assertThat(logTester.logs(LoggerLevel.ERROR).size()).isEqualTo(1);
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0)).startsWith("Stack overflow");
  }

  @Test
  public void test_analyze_test_file() throws Exception {
    PHPCheck check = new DummyCheck();
    PHPCheck testCheck = new DummyCheck();
    PHPAnalyzer analyzer = new PHPAnalyzer(Arrays.asList(check, testCheck), Collections.singletonList(testCheck), tmpFolder.newFolder(), new ProjectSymbolData());
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php $a = 1;");
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
  public void test_cpd() throws Exception {
    PHPAnalyzer analyzer = createAnalyzer();
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php $a = 1;");
    analyzer.nextFile(file);

    assertThat(analyzer.computeCpdTokens()).hasSize(4);
  }

  @Test
  public void terminate_call_forwarded_to_checks() throws Exception {
    PHPCheck check1 = spy(new DummyCheck());
    PHPCheck check2 = spy(new DummyCheck());
    PHPAnalyzer analyzer = createAnalyzer(check1, check2);
    analyzer.terminate();

    verify(check1).terminate();
    verify(check2).terminate();
  }

  @Test
  public void log_error_and_continue_when_exception_in_terminate() throws Exception {
    PHPCheck check1 = spy(new DummyCheck());
    doThrow(new RuntimeException("myError")).when(check1).terminate();
    PHPCheck check2 = spy(new DummyCheck());
    PHPAnalyzer analyzer = createAnalyzer(check1, check2);
    analyzer.terminate();

    verify(check1).terminate();
    verify(check2).terminate();
    assertThat(logTester.logs(LoggerLevel.WARN)).contains("An error occurred while trying to terminate checks:");
  }

  private PHPAnalyzer createAnalyzer(PHPCheck... checks) throws IOException {
    return new PHPAnalyzer(Arrays.asList(checks), tmpFolder.newFolder(), new ProjectSymbolData());
  }
}
