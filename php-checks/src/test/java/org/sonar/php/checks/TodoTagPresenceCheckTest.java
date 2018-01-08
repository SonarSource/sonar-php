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
import java.util.List;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class TodoTagPresenceCheckTest {

  private static final PHPCheck CHECK = new TodoTagPresenceCheck();

  @Test
  public void test() throws Exception {
    PhpFile file = TestUtils.getCheckFile("TodoTagPresenceCheck.php");

    List<PhpIssue> issues = ImmutableList.of(
      newIssue(4),
      newIssue(8),
      newIssue(9),
      newIssue(12),
      newIssue(14),
      newIssue(16));

    PHPCheckTest.check(CHECK, file, issues);
  }

  private PhpIssue newIssue(int line) {
    String message = "Complete the task associated to this \"TODO\" comment.";
    return new LegacyIssue(CHECK, message).line(line);
  }
}
