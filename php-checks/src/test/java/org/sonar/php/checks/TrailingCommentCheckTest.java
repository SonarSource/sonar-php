/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class TrailingCommentCheckTest {

  private TrailingCommentCheck check = new TrailingCommentCheck();
  private String fileName = "TrailingCommentCheck.php";

  @Test
  void defaultValue() throws Exception {
    List<PhpIssue> issues = Arrays.asList(
      newIssue(4),
      newIssue(5));

    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName), issues);
  }

  @Test
  void custom() throws Exception {
    check.legalCommentPattern = "";
    List<PhpIssue> issues = Arrays.asList(
      newIssue(4),
      newIssue(5),
      newIssue(11),
      newIssue(12));
    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName), issues);
  }

  private PhpIssue newIssue(int line) {
    String message = "Move this trailing comment on the previous empty line.";
    return new LineIssue(check, line, message);
  }
}
