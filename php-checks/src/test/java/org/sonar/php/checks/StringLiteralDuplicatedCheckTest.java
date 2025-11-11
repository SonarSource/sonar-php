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
package org.sonar.php.checks;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class StringLiteralDuplicatedCheckTest {

  private StringLiteralDuplicatedCheck check = new StringLiteralDuplicatedCheck();

  @Test
  void defaultValue() {
    CheckVerifier.verify(check, "StringLiteralDuplicatedCheck/default.php");
  }

  @Test
  void customPropertyThreshold() {
    check.threshold = 4;
    CheckVerifier.verify(check, "StringLiteralDuplicatedCheck/custom_threshold.php");
  }

  @Test
  void customPropertyMinimalLiteralLength() {
    check.minimalLiteralLength = 4;
    CheckVerifier.verify(check, "StringLiteralDuplicatedCheck/custom_length.php");
  }

  @Test
  void laravelValidation() {
    CheckVerifier.verify(check, "StringLiteralDuplicatedCheck/laravel_validation.php");
  }

  @Test
  void drupalForms() {
    CheckVerifier.verify(check, "StringLiteralDuplicatedCheck/drupal_forms.php");
  }

  @Test
  void arrayKeys() {
    CheckVerifier.verify(check, "StringLiteralDuplicatedCheck/array_keys.php");
  }

  @Test
  void ignoreImportmapFile() {
    CheckVerifier.verifyNoIssue(check, "StringLiteralDuplicatedCheck/importmap.php");
  }
}
