/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.ini;

import org.junit.jupiter.api.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.php.ini.BasePhpIniIssue.newIssue;

class BasePhpIniIssueTest {

  @Test
  void createNewIssue() {
    PhpIniIssue issue = newIssue("message1").line(42);
    assertThat(issue.message()).isEqualTo("message1");
    assertThat(issue.line()).isEqualTo(42);
  }

}
