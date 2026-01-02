/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CounterVisitorTest extends ParsingTestUtils {

  @Test
  void testClass() {
    CounterVisitor counterVisitor = new CounterVisitor(parse("metrics/classes.php"));
    assertThat(counterVisitor.getClassNumber()).isEqualTo(4);
  }

  @Test
  void testStatements() {
    CounterVisitor counterVisitor = new CounterVisitor(parse("metrics/statements.php"));
    assertThat(counterVisitor.getStatementNumber()).isEqualTo(29);
  }

  @Test
  void testFunctions() {
    CounterVisitor counterVisitor = new CounterVisitor(parse("metrics/functions.php"));
    assertThat(counterVisitor.getFunctionNumber()).isEqualTo(4);
  }

}
