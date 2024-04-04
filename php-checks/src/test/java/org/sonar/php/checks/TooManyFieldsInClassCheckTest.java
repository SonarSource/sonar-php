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

class TooManyFieldsInClassCheckTest {

  private TooManyFieldsInClassCheck check = new TooManyFieldsInClassCheck();
  private static final String FILE_NAME = "TooManyFieldsInClassCheck.php";

  @Test
  void testDefault() {
    CheckVerifier.verifyNoIssueIgnoringExpected(check, FILE_NAME);
  }

  @Test
  void testCustomMaximumFieldThreshold() {
    check.maximumFieldThreshold = 4;
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void testCustomCountNonpublicFields() {
    check.maximumFieldThreshold = 2;
    check.countNonpublicFields = false;

    List<PhpIssue> issues = Arrays.asList(
      new LineIssue(check, 3, "Refactor this class so it has no more than 2 public fields, rather than the 3 it currently has."),
      new LineIssue(check, 18, "Refactor this class so it has no more than 2 public fields, rather than the 3 it currently has."),
      new LineIssue(check, 33, "Refactor this class so it has no more than 2 public fields, rather than the 5 it currently has."),
      new LineIssue(check, 43, "Refactor this class so it has no more than 2 public fields, rather than the 5 it currently has."),
      new LineIssue(check, 83, "Refactor this class so it has no more than 2 public fields, rather than the 5 it currently has."),
      new LineIssue(check, 160, "Refactor this class so it has no more than 2 public fields, rather than the 5 it currently has."),
      new LineIssue(check, 175, "Refactor this class so it has no more than 2 public fields, rather than the 5 it currently has."),
      new LineIssue(check, 190, "Refactor this class so it has no more than 2 public fields, rather than the 5 it currently has."));
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), issues);
  }
}
