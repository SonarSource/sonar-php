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
package org.sonar.php.checks.wordpress;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WordPressExternalRequestsCheckTest extends WordPressConfigCheckTest {

  @TempDir
  public static File tmpDir;
  private final WordPressExternalRequestsCheck check = new WordPressExternalRequestsCheck();

  public WordPressExternalRequestsCheckTest() {
    super(tmpDir);
  }

  @Test
  void test() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressExternalRequestsCheck/test.php");
  }

  @Test
  void testAbsence() throws IOException {
    wordPressVerifier.verifyAbsence(check, "Make sure allowing external requests is intended.");
  }
}
