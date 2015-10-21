/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.metrics;

import org.junit.Test;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.ParsingTestUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricsVisitorTest extends ParsingTestUtils {

  @Test
  public void test() {
    String filename = "metrics/lines_of_code.php";
    File file = new File(filename);

    FileMeasures fileMeasures = new MetricsVisitor().getFileMeasures(file, parse(filename), mock(FileLinesContext.class));

    assertThat(fileMeasures.getFileComplexity()).isEqualTo(1);
    assertThat(fileMeasures.getClassComplexity()).isEqualTo(1);
    assertThat(fileMeasures.getFunctionComplexity()).isEqualTo(1);

    assertThat(fileMeasures.getFileComplexityDistribution().build().getData()).isEqualTo("0=1;5=0;10=0;20=0;30=0;60=0;90=0");
    assertThat(fileMeasures.getFunctionComplexityDistribution().build().getData()).isEqualTo("1=1;2=0;4=0;6=0;8=0;10=0;12=0");

    assertThat(fileMeasures.getFunctionNumber()).isEqualTo(1);
    assertThat(fileMeasures.getStatementNumber()).isEqualTo(2);
    assertThat(fileMeasures.getClassNumber()).isEqualTo(1);

    assertThat(fileMeasures.getLinesNumber()).isEqualTo(29);
    assertThat(fileMeasures.getLinesOfCodeNumber()).isEqualTo(7);

    assertThat(fileMeasures.getNoSonarLines()).containsOnly(18);
    assertThat(fileMeasures.getCommentLinesNumber()).isEqualTo(5);
  }
}
