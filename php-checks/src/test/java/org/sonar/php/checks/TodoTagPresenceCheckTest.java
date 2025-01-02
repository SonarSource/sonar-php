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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class TodoTagPresenceCheckTest {

  private static final PHPCheck CHECK = new TodoTagPresenceCheck();

  @Test
  void test() throws Exception {
    PhpFile file = TestUtils.getCheckFile("TodoTagPresenceCheck.php");

    List<PhpIssue> issues = Arrays.asList(
      newIssue(4),
      newIssue(8),
      newIssue(9),
      newIssue(12),
      newIssue(14),
      newIssue(16));

    PHPCheckTest.check(CHECK, file, issues);
  }

  private static PhpIssue newIssue(int line) {
    String message = "Complete the task associated to this \"TODO\" comment.";
    return new LineIssue(CHECK, line, message);
  }
}
