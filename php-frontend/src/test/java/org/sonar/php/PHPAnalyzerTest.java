/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.RecognitionException;
import java.io.IOException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.metrics.FileMeasures;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PHPAnalyzerTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test(expected = RecognitionException.class)
  public void parsing_failure_should_raise_an_exception() throws IOException {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = new PHPAnalyzer(ImmutableList.of(check));
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php if(condition): ?>");
    analyzer.nextFile(file);
  }

  @Test
  public void test_analyze() throws Exception {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = new PHPAnalyzer(ImmutableList.of(check));
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
  public void test_cpd() throws Exception {
    PHPAnalyzer analyzer = new PHPAnalyzer(ImmutableList.of());
    PhpFile file = FileTestUtils.getFile(tmpFolder.newFile(), "<?php $a = 1;");
    analyzer.nextFile(file);

    assertThat(analyzer.computeCpdTokens()).hasSize(4);
  }
}
