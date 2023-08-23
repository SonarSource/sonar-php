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

class MissingNewLineAtEOFCheckTest {

  private MissingNewLineAtEOFCheck check = new MissingNewLineAtEOFCheck();
  private static final String TEST_FILE_DIR = "MissingNewLineAtEOF/";
  private List<PhpIssue> issue = Collections.singletonList(
    new FileIssue(check, "Add a new line at the end of this file."));

  @Test
  void noNewLine() {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_FILE_DIR + "MissingNewLineAtEOF.php"), issue);
  }

  @Test
  void emptyFile() {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_FILE_DIR + "EmptyFile.php"), issue);
  }

  @Test
  void newLine() {
    CheckVerifier.verifyNoIssue(check, TEST_FILE_DIR + "NewLineAtEOF.php");
  }

}
