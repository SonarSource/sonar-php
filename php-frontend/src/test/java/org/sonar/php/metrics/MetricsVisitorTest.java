/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.php.metrics;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.FileTestUtils;
import org.sonar.php.compat.CompatibleInputFile;
import org.sonar.test.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MetricsVisitorTest extends ParsingTestUtils {

  @Test
  public void test() {
    String filename = "metrics/lines_of_code.php";
    CompatibleInputFile file = FileTestUtils.getFile(TestUtils.getResource(filename));

    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    FileMeasures fileMeasures = new MetricsVisitor().getFileMeasures(file, parse(filename), fileLinesContext, true);

    assertThat(fileMeasures.getFileComplexity()).isEqualTo(1);
    assertThat(fileMeasures.getClassComplexity()).isEqualTo(1);
    assertThat(fileMeasures.getFunctionComplexity()).isEqualTo(1);

    assertThat(fileMeasures.getFileComplexityDistribution().build()).isEqualTo("0=1;5=0;10=0;20=0;30=0;60=0;90=0");
    assertThat(fileMeasures.getFunctionComplexityDistribution().build()).isEqualTo("1=1;2=0;4=0;6=0;8=0;10=0;12=0");

    assertThat(fileMeasures.getFunctionNumber()).isEqualTo(1);
    assertThat(fileMeasures.getStatementNumber()).isEqualTo(2);
    assertThat(fileMeasures.getClassNumber()).isEqualTo(1);

    assertThat(fileMeasures.getLinesNumber()).isEqualTo(29);
    assertThat(fileMeasures.getLinesOfCodeNumber()).isEqualTo(7);

    assertThat(fileMeasures.getNoSonarLines()).containsOnly(18);
    assertThat(fileMeasures.getCommentLinesNumber()).isEqualTo(5);

    verify(fileLinesContext).setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, 1, 0);
    verify(fileLinesContext).setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, 13, 1);
  }

  @Test
  public void dont_save_executable_lines() {
    String filename = "metrics/lines_of_code.php";
    CompatibleInputFile file = FileTestUtils.getFile(TestUtils.getResource(filename));

    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    new MetricsVisitor().getFileMeasures(file, parse(filename), fileLinesContext,  false);

    verify(fileLinesContext, never()).setIntValue(Mockito.eq(CoreMetrics.EXECUTABLE_LINES_DATA_KEY), Mockito.anyInt(), Mockito.anyInt());
  }

}
