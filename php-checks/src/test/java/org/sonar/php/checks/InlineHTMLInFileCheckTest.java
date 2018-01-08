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
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.FileIssue;

public class InlineHTMLInFileCheckTest {

  private InlineHTMLInFileCheck check = new InlineHTMLInFileCheck();
  private final String TEST_DIR = "InlineHTMLInFileCheck/";

  @Test
  public void ok() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.php");
  }

  @Test
  public void ok_asp() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_asp.php");
  }

  @Test
  public void ok_excluded_file() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.phtml");
  }

  @Test
  public void ko() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko.php"), ImmutableList.of(new FileIssue(check, "Remove the inline HTML in this file.")));
  }
}
