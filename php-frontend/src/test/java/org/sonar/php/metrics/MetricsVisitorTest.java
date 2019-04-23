/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import java.io.File;
import org.junit.Test;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MetricsVisitorTest extends ParsingTestUtils {

  @Test
  public void test() {
    String filename = "metrics/lines_of_code.php";
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/"+filename));

    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    FileMeasures fileMeasures = new MetricsVisitor().getFileMeasures(file, parse(filename), fileLinesContext);

    assertThat(fileMeasures.getFileComplexity()).isEqualTo(1);
    assertThat(fileMeasures.getFileCognitiveComplexity()).isEqualTo(0);

    assertThat(fileMeasures.getFunctionNumber()).isEqualTo(1);
    assertThat(fileMeasures.getStatementNumber()).isEqualTo(2);
    assertThat(fileMeasures.getClassNumber()).isEqualTo(1);

    assertThat(fileMeasures.getLinesOfCodeNumber()).isEqualTo(7);

    assertThat(fileMeasures.getCommentLinesNumber()).isEqualTo(5);

    verify(fileLinesContext).setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, 21, 1);
  }

}
