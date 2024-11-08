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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class FileNameCheckTest {

  private FileNameCheck check = new FileNameCheck();
  private static final String TEST_DIR = "FileNameCheck/";

  @Test
  void okDefaultValue() {
    checkNoIssue("ok.php");
  }

  @Test
  void koDefaultValue() {
    checkIssue("_ko.php", "Rename this file to match this regular expression: \"" + FileNameCheck.DEFAULT + "\"");
  }

  @Test
  void okCustom() {
    check.format = "_[a-z][A-Za-z0-9]+.php";
    checkNoIssue("_ko.php");
  }

  @Test
  void koCustom() {
    check.format = "_[a-z][A-Za-z0-9]+.php";
    checkIssue("ok.php", "Rename this file to match this regular expression: \"" + check.format + "\"");
  }

  private void checkNoIssue(String fileName) {
    check(fileName, Collections.emptyList());
  }

  private void checkIssue(String fileName, String expectedIssueMessage) {
    check(fileName, Collections.singletonList(new FileIssue(check, expectedIssueMessage)));
  }

  private void check(String fileName, List<PhpIssue> expectedIssues) {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + fileName), expectedIssues);
  }

}
