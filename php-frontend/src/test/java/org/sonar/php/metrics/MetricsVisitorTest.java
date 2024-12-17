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
package org.sonar.php.metrics;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MetricsVisitorTest extends ParsingTestUtils {

  @Test
  void test() {
    String filename = "metrics/lines_of_code.php";
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/" + filename));

    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    CompilationUnitTree ast = parse(filename);
    FileMeasures fileMeasures = new MetricsVisitor().getFileMeasures(file, ast, SymbolTableImpl.create(ast), fileLinesContext);

    assertThat(fileMeasures.getFileComplexity()).isEqualTo(1);
    assertThat(fileMeasures.getFileCognitiveComplexity()).isZero();

    assertThat(fileMeasures.getFunctionNumber()).isEqualTo(1);
    assertThat(fileMeasures.getStatementNumber()).isEqualTo(2);
    assertThat(fileMeasures.getClassNumber()).isEqualTo(1);

    assertThat(fileMeasures.getLinesOfCodeNumber()).isEqualTo(7);

    assertThat(fileMeasures.getCommentLinesNumber()).isEqualTo(5);

    verify(fileLinesContext).setIntValue(CoreMetrics.EXECUTABLE_LINES_DATA_KEY, 21, 1);

    verifyNCLOCDataMetric(fileLinesContext, 29, 13, 17, 19, 20, 21, 22, 23);
  }

  protected void verifyNCLOCDataMetric(FileLinesContext fileLinesContext, Integer sizeOfFile, Integer... linesOfCode) {
    var linesOfCodeSet = Arrays.stream(linesOfCode).collect(Collectors.toSet());
    for (var lineNumber = 1; lineNumber <= sizeOfFile; lineNumber++) {
      if (linesOfCodeSet.contains(lineNumber)) {
        verify(fileLinesContext).setIntValue(CoreMetrics.NCLOC_DATA_KEY, lineNumber, 1);
      } else {
        verify(fileLinesContext, never()).setIntValue(CoreMetrics.NCLOC_DATA_KEY, lineNumber, 1);
      }
    }
  }

}
