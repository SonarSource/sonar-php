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

import java.net.URISyntaxException;

public class FileNameCheckTest {

  private FileNameCheck check = new FileNameCheck();
  private static final String TEST_DIR = "FileNameCheck/";

  @Test
  public void ok_defaultValue() throws Exception {
    checkNoIssue("ok.php");
  }

  @Test
  public void ko_defaultValue() throws Exception {
    checkIssue("_ko.php", "Rename this file to match this regular expression: \"" + FileNameCheck.DEFAULT + "\"");
  }

  @Test
  public void ok_custom() throws Exception {
    check.format = "_[a-z][A-Za-z0-9]+.php";
    checkNoIssue("_ko.php");
  }

  @Test
  public void ko_custom() throws Exception {
    check.format = "_[a-z][A-Za-z0-9]+.php";
    checkIssue("ok.php", "Rename this file to match this regular expression: \"" + check.format + "\"");
  }

  private void checkNoIssue(String fileName) throws URISyntaxException {
    check(fileName, ImmutableList.<PhpIssue>of());
  }

  private void checkIssue(String fileName, String expectedIssueMessage) throws URISyntaxException {
    check(fileName, ImmutableList.<PhpIssue>of(new LegacyIssue(check, expectedIssueMessage)));
  }

  private void check(String fileName, ImmutableList<PhpIssue> expectedIssues) throws URISyntaxException {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + fileName), expectedIssues);
  }

}
