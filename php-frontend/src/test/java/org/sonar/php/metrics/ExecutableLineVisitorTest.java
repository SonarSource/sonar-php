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
package org.sonar.php.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutableLineVisitorTest extends ParsingTestUtils {

  @Test
  void test() {
    String filename = "metrics/executable_lines.php";
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/" + filename));
    Set<Integer> executableLines = new ExecutableLineVisitor(parse(filename)).getExecutableLines();
    assertThat(executableLines).hasSameElementsAs(expectedExecutableLines(file));
  }

  // returns lines marked with "// +1" comment
  private static Set<Integer> expectedExecutableLines(PhpFile phpFile) {
    Set<Integer> expectedExecutableLines = new HashSet<>();

    List<String> lines = new BufferedReader(new StringReader(phpFile.contents())).lines().toList();
    for (int i = 0; i < lines.size(); i++) {
      if (lines.get(i).contains("// +1")) {
        expectedExecutableLines.add(i + 1);
      }
    }
    return expectedExecutableLines;
  }

}
