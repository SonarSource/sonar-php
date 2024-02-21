/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class TooManyMethodsInClassCheckTest {

  private TooManyMethodsInClassCheck check = new TooManyMethodsInClassCheck();
  private static final String FILE_NAME = "TooManyMethodsInClassCheck.php";

  @Test
  void defaultValue() throws Exception {
    CheckVerifier.verifyNoIssueIgnoringExpected(check, FILE_NAME);
  }

  @Test
  void customMaximumMethodThreshold() throws Exception {
    check.maximumMethodThreshold = 2;
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void customCountNonPublicMethod() throws Exception {
    check.maximumMethodThreshold = 2;
    check.countNonpublicMethods = false;

    List<PhpIssue> issues = Arrays.asList(
      new LineIssue(check, 3, "Class \"I\" has 3 methods, which is greater than 2 authorized. Split it into smaller classes."),
      new LineIssue(check, 35, "This anonymous class has 3 methods, which is greater than 2 authorized. Split it into smaller classes."));
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues);
  }

}
