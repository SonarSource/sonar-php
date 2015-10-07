/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.php.tree.visitors.PHPIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.Issue;

import java.io.File;
import java.util.List;

public class FixmeTagPresenceCheckTest {

  @Test
  public void test() throws Exception {
    File file = TestUtils.getCheckFile("FixmeTagPresenceCheck.php");

    List<Issue> issues = ImmutableList.of(
      newIssue(4),
      newIssue(8),
      newIssue(9),
      newIssue(12),
      newIssue(14)
    );

    PHPCheckTest.check(new FixmeTagPresenceCheck(), file, issues);
  }

  private Issue newIssue(int line) {
    String message = "Take the required action to fix the issue indicated by this \"FIXME\" comment.";
    return new PHPIssue("testKey", message).line(line);
  }

}
