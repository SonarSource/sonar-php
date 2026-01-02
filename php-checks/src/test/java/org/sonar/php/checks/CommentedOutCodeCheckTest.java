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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class CommentedOutCodeCheckTest {

  private static final PHPCheck CHECK = new CommentedOutCodeCheck();

  @Test
  void test() throws Exception {
    List<PhpIssue> issues = Arrays.asList(
      newIssue(13),
      newIssue(18),
      newIssue(31),
      newIssue(33),
      newIssue(39),
      newIssue(56),
      newIssue(60),
      newIssue(75),
      newIssue(79),
      newIssue(85),
      newIssue(87),
      newIssue(93),
      newIssue(97));

    PHPCheckTest.check(CHECK, TestUtils.getCheckFile("CommentedOutCodeCheck.php"), issues);
  }

  private static PhpIssue newIssue(int line) {
    String message = "Remove this commented out code.";
    return new LineIssue(CHECK, line, message);
  }
}
