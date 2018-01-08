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
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class FileWithSymbolsAndSideEffectsCheckTest {

  private final String TEST_DIR = "FileWithSymbolsAndSideEffectsCheck/";
  private final FileWithSymbolsAndSideEffectsCheck check = new FileWithSymbolsAndSideEffectsCheck();

  private final ImmutableList<PhpIssue> issue = ImmutableList.<PhpIssue>of(new LegacyIssue(check, "Refactor this file to either declare symbols or cause side effects, but not both."));


  @Test
  public void ok() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.php");
  }

  @Test
  public void ok_with_define_and_closing_html() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_define.php");
  }

  @Test
  public void ok_no_symbol() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_no_symbol.php");
  }

  @Test
  public void ko_echo() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_echo.php"), issue);
  }

  @Test
  public void ko_expression_statement() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_expression_statement.php"), issue);
  }

  @Test
  public void ko_yield() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_yield.php"), issue);
  }

  @Test
  public void ko_inline_html() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_inline_html.php"), issue);
  }

  @Test
  public void ko_unset_variable() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_unset_variable.php"), issue);
  }

}
