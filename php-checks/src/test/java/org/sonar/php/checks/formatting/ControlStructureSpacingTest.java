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

public class ControlStructureSpacingTest extends FormattingStandardCheckTest {

  private static String TEST_FILE = TEST_DIR + "ControlStructureSpacingCheck.php";

  @Test
  public void defaultValue() throws IllegalAccessException {
    activeOnly("isOneSpaceBetweenKeywordAndNextToken", "isOneSpaceAfterForLoopSemicolon", "isSpaceForeachStatement");
    CheckVerifier.verify(check, TEST_FILE);
  }

  @Test
  public void custom() throws IllegalAccessException {
    deactivateAll();
    CheckVerifier.verifyNoIssueIgnoringExpected(check, TEST_FILE);
  }
}
