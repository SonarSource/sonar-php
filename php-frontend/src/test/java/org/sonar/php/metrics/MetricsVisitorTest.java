/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricsVisitorTest extends ParsingTestUtils {

  @Test
  public void test() {
    String filename = "metrics/lines_of_code.php";
    File file = new File(filename);
    Map<File, Integer> numberOfLinesOfCode = new HashMap<File, Integer>();

    FileMeasures fileMeasures = new MetricsVisitor().getFileMeasures(file, parse(filename), mock(FileLinesContext.class), numberOfLinesOfCode);

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
    
    assertThat(numberOfLinesOfCode.values().iterator().next()).as("number of lines of code in the file").isEqualTo(7);
  }

}
