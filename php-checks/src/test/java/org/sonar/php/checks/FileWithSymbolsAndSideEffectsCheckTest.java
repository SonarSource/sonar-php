/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class FileWithSymbolsAndSideEffectsCheckTest {

  private static final String TEST_DIR = "FileWithSymbolsAndSideEffectsCheck/";
  private final FileWithSymbolsAndSideEffectsCheck check = new FileWithSymbolsAndSideEffectsCheck();

  private final List<PhpIssue> issue = Collections.singletonList(
    new FileIssue(check, "Refactor this file to either declare symbols or cause side effects, but not both."));

  @Test
  void ok() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.php");
  }

  @Test
  void okWithDefineAndClosingHtml() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_define.php");
  }

  @Test
  void okNoSymbol() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_no_symbol.php");
  }

  @Test
  void koEcho() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_echo.php"), issue);
  }

  @Test
  void koExpressionStatement() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_expression_statement.php"), issue);
  }

  @Test
  void koYield() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_yield.php"), issue);
  }

  @Test
  void koInlineHtml() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_inline_html.php"), issue);
  }

  @Test
  void koUnsetVariable() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko_unset_variable.php"), issue);
  }

}
