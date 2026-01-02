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
package org.sonar.php.checks.wordpress;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class WordPressConfigNameCheckTest {

  @Test
  void test() {
    CheckVerifier.verify(new WordPressConfigNameCheck(), "wordpress/WordPresConfigNameCheck/wp-config.php");
  }

  @Test
  void customOption() {
    WordPressConfigNameCheck check = new WordPressConfigNameCheck();
    check.customOptions = "MY_CUSTOM_OPTION, MY_SECOND_CUSTOM_OPTION";
    CheckVerifier.verify(check, "wordpress/WordPresConfigNameCheck/custom/wp-config.php");
  }
}
