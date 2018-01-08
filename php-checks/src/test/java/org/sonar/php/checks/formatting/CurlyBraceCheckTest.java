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
package org.sonar.php.checks.formatting;

import org.junit.Test;
import org.sonar.php.checks.FormattingStandardCheckTest;
import org.sonar.plugins.php.CheckVerifier;

public class CurlyBraceCheckTest extends FormattingStandardCheckTest {

  @Test
  public void defaultValue() throws Exception {
    activeOnly("isOpenCurlyBraceForClassAndFunction", "isOpenCurlyBraceForControlStructures", "isClosingCurlyNextToKeyword");
    CheckVerifier.verify(check, TEST_DIR + "CurlyBraceCheck.php");
  }

  @Test
  public void custom() throws Exception {
    deactivateAll();
    CheckVerifier.verifyNoIssueIgnoringExpected(check, TEST_DIR + "CurlyBraceCheck.php");
  }

  @Test
  public void closing_curly() throws Exception {
    activeOnly("isClosingCurlyNextToKeyword");
    CheckVerifier.verify(check, TEST_DIR + "ClosingCurlyBraceCheck.php");
  }
}
