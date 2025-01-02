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
package org.sonar.plugins.php.api.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.php.utils.DummyCheck;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LineIssueTest {

  private static final PHPCheck CHECK = new DummyCheck();

  @Test
  void test() {
    LineIssue lineIssue = new LineIssue(CHECK, 42, "Test message");

    assertThat(lineIssue.check()).isEqualTo(CHECK);
    assertThat(lineIssue.cost()).isNull();
    assertThat(lineIssue.line()).isEqualTo(42);
    assertThat(lineIssue.message()).isEqualTo("Test message");
  }

  @Test
  void withCost() {
    LineIssue lineIssue = new LineIssue(CHECK, 42, "Test message").cost(5);

    assertThat(lineIssue.check()).isEqualTo(CHECK);
    assertThat(lineIssue.cost()).isEqualTo(5);
    assertThat(lineIssue.line()).isEqualTo(42);
    assertThat(lineIssue.message()).isEqualTo("Test message");
  }
}
