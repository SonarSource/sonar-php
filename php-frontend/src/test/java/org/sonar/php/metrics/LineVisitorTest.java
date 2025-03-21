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

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

class LineVisitorTest extends ParsingTestUtils {

  private static final String LOC_FILE = "metrics/lines_of_code.php";

  @Test
  void testLinesOfCodeNumber() {
    LineVisitor lineVisitor = new LineVisitor(parse(LOC_FILE));
    assertThat(lineVisitor.getLinesOfCodeNumber()).isEqualTo(7);
  }

  @Test
  void testLinesOfCodeNumberOnTree() {
    CompilationUnitTree cut = parse(LOC_FILE);
    Optional<ClassDeclarationTree> firstClassTree = cut.script().statements().stream()
      .filter(statement -> statement.is(Tree.Kind.CLASS_DECLARATION))
      .map(ClassDeclarationTree.class::cast)
      .findFirst();
    assertThat(firstClassTree).isPresent();
    assertThat(LineVisitor.linesOfCode(firstClassTree.get())).isEqualTo(4);
  }

  @Test
  void testLinesOfCode() {
    LineVisitor lineVisitor = new LineVisitor(parse(LOC_FILE));
    Set<Integer> linesOfCode = lineVisitor.getLinesOfCode();
    assertThat(linesOfCode).hasSize(7).contains(13, 17, 19, 20, 21, 22, 23);
  }

}
