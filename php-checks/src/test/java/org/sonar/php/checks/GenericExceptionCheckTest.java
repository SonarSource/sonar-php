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

class GenericExceptionCheckTest {

  private GenericExceptionCheck check = new GenericExceptionCheck();
  private static final String TEST_DIR = "GenericExceptionCheck/";

  @Test
  void okNonNamespace() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok1.php");
  }

  @Test
  void okNamespace() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok2.php");
  }

  @Test
  void koNonNamespace() throws Exception {
    CheckVerifier.verify(check, TEST_DIR + "ko1.php");
  }

  @Test
  void koNamespace() throws Exception {
    CheckVerifier.verify(check, TEST_DIR + "ko2.php");
  }

  @Test
  void koNamespaceUse() throws Exception {
    CheckVerifier.verify(check, TEST_DIR + "ko3.php");
  }

  @Test
  void koMultipleNamespaces() throws Exception {
    CheckVerifier.verify(check, TEST_DIR + "ko4.php");
  }

  @Test
  void throwExpressions() throws Exception {
    CheckVerifier.verify(check, TEST_DIR + "throw_expressions.php");
  }
}
