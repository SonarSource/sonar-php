/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.wordpress;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.visitors.PHPCheck;

class WordPressLateConfigCheckTest extends WordPressConfigCheckTest {

  @TempDir
  public static File tmpDir;

  PHPCheck check = new WordPressLateConfigCheck();

  public WordPressLateConfigCheckTest() {
    super(tmpDir);
  }

  @Test
  void testConfigFileWithParenthesis() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressLateConfigCheck/with-parenthesis.php");
  }

  @Test
  void testConfigFileWithoutParenthesis() throws IOException {
    wordPressVerifier.verify(check, "wordpress/WordPressLateConfigCheck/without-parenthesis.php");
  }

  @Test
  void testOtherFile() {
    CheckVerifier.verifyNoIssue(check, "wordpress/WordPressLateConfigCheck/no-wp-config.php");
  }

}
