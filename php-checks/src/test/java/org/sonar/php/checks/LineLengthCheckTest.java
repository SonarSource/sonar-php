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
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class LineLengthCheckTest {

  private LineLengthCheck check = new LineLengthCheck();

  @Test
  void defaultValue() throws Exception {
    List<PhpIssue> issues = Collections.singletonList(
      new LineIssue(check, 4, "Split this 122 characters long line (which is greater than 120 authorized)."));

    PHPCheckTest.check(check, TestUtils.getCheckFile("LineLengthCheck.php"), issues);
  }

  @Test
  void custom() throws Exception {
    check.maximumLineLength = 30;
    List<PhpIssue> issues = Arrays.asList(
      new LineIssue(check, 4, "Split this 122 characters long line (which is greater than 30 authorized)."),
      new LineIssue(check, 5, "Split this 42 characters long line (which is greater than 30 authorized)."));

    PHPCheckTest.check(check, TestUtils.getCheckFile("LineLengthCheck.php"), issues);
  }
}
