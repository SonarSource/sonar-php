/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class FunctionNameCheckTest {

  private static final String FILE_NAME = "FunctionNameCheck.php";
  private static final String FILE_NAME_DRUPAL = "FunctionNameCheckDrupal.php";
  private static final String FILE_NAME_DRUPAL_CUSTOM_REGEX = "FunctionNameCheckDrupalWithCustomRegex.php";
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
}
