/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;

import java.util.List;

public class CommentedOutCodeCheckTest {

  private static final PHPCheck CHECK = new CommentedOutCodeCheck();

  @Test
  public void test() throws Exception {
    List<PhpIssue> issues = ImmutableList.of(
      newIssue(9),
      newIssue(16),
      newIssue(25)
    );

    PHPCheckTest.check(CHECK, TestUtils.getCheckFile("CommentedOutCodeCheck.php"), issues);
  }

  private PhpIssue newIssue(int line) {
    String message = "Remove this commented out code.";
    return new LegacyIssue(CHECK, message).line(line);
  }
}
