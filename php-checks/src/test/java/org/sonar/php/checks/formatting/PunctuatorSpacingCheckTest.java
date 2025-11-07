/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.formatting;

import org.junit.jupiter.api.Test;
import org.sonar.php.checks.FormattingStandardCheckTest;
import org.sonar.plugins.php.CheckVerifier;

class PunctuatorSpacingCheckTest extends FormattingStandardCheckTest {

  private static final String TEST_FILE = TEST_DIR + "PunctuatorSpacingCheck.php";

  @Test
  void defaultValue() throws IllegalAccessException {
    activeOnly("isOneSpaceBetweenRParentAndLCurly", "isNoSpaceParenthesis");
    CheckVerifier.verify(check, TEST_FILE);
  }

  @Test
  void custom() throws IllegalAccessException {
    deactivateAll();
    CheckVerifier.verifyNoIssueIgnoringExpected(check, TEST_FILE);
  }

}
