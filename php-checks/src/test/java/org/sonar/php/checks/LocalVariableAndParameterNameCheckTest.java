/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.checks;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class LocalVariableAndParameterNameCheckTest {

  private static final String FILE_NAME = "LocalVariableAndParameterNameCheck.php";
  private final LocalVariableAndParameterNameCheck check = new LocalVariableAndParameterNameCheck();

  @Test
  void defaultValue() {
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void custom() {
    check.format = "^[A-Z_a-z0-9]*$";
    CheckVerifier.verifyNoIssueIgnoringExpected(check, FILE_NAME);
  }
}
