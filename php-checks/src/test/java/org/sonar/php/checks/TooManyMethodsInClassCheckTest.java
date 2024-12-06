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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class TooManyMethodsInClassCheckTest {

  private TooManyMethodsInClassCheck check = new TooManyMethodsInClassCheck();
  private static final String FILE_NAME = "TooManyMethodsInClassCheck.php";

  @Test
  void shouldReportNoIssueOnDefaultValue() {
    CheckVerifier.verifyNoIssueIgnoringExpected(check, FILE_NAME);
  }

  @Test
  void shouldReportIssuesWithCustomThreshold() {
    check.maximumMethodThreshold = 2;
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void shouldReportIssuesWithCustomThresholdAndWithoutNonPublicMethods() {
    check.maximumMethodThreshold = 2;
    check.countNonpublicMethods = false;

    List<PhpIssue> issues = Arrays.asList(
      new LineIssue(check, 3, "Class \"I\" has 3 methods, which is greater than 2 authorized. Split it into smaller classes."),
      new LineIssue(check, 44, "This anonymous class has 3 methods, which is greater than 2 authorized. Split it into smaller classes."));
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues);
  }

}
