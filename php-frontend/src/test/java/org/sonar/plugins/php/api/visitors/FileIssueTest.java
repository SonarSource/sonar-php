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
package org.sonar.plugins.php.api.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.php.utils.DummyCheck;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileIssueTest {

  private static final PHPCheck CHECK = new DummyCheck();

  @Test
  void test() {
    FileIssue fileIssue = new FileIssue(CHECK, "Test message");

    assertThat(fileIssue.check()).isEqualTo(CHECK);
    assertThat(fileIssue.cost()).isNull();
    assertThat(fileIssue.message()).isEqualTo("Test message");
  }

  @Test
  void withCost() {
    FileIssue fileIssue = new FileIssue(CHECK, "Test message").cost(5);

    assertThat(fileIssue.check()).isEqualTo(CHECK);
    assertThat(fileIssue.cost()).isEqualTo(5);
    assertThat(fileIssue.message()).isEqualTo("Test message");
  }
}
