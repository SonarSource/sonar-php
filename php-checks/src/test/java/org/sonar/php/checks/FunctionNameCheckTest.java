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

class FunctionNameCheckTest {

  private static final String FILE_NAME = "FunctionNameCheck.php";
  private static final String FILE_NAME_DRUPAL = "FunctionNameCheckDrupal.php";
  private static final String FILE_NAME_DRUPAL_CUSTOM_REGEX = "FunctionNameCheckDrupalWithCustomRegex.php";
  private static final String FILE_NAME_WORDPRESS = "FunctionNameCheckWordpress.php";
  private FunctionNameCheck check = new FunctionNameCheck();

  @Test
  void testDefaultValue() {
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void testCustom() {
    check.format = "^[a-zA-Z][a-zA-Z0-9]*$";
    CheckVerifier.verifyNoIssueIgnoringExpected(check, FILE_NAME);
  }

  @Test
  void testDefaultValueDrupal() {
    CheckVerifier.verify(check, FILE_NAME_DRUPAL);
  }

  @Test
  void testCustomDrupal() {
    check.format = "^[a-zA-Z][a-zA-Z0-9_]*$";
    CheckVerifier.verifyNoIssueIgnoringExpected(check, FILE_NAME_DRUPAL);
  }

  @Test
  void customShouldPreventDrupalOverride() {
    check.format = "^[a-zA-Z][a-zA-Z0-9]*$";
    CheckVerifier.verify(check, FILE_NAME_DRUPAL_CUSTOM_REGEX);
  }

  @Test
  void testDefaultValueWordpress() {
    CheckVerifier.verify(check, FILE_NAME_WORDPRESS);
  }
}
