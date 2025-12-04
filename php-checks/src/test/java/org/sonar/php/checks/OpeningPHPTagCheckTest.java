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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.php.CheckVerifier;

class OpeningPHPTagCheckTest {

  private final OpeningPHPTagCheck check = new OpeningPHPTagCheck();
  private static final String TEST_DIR = "OpeningPHPTagCheck/";

  @Test
  void okLongTag() {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_long_tag.php");
  }

  @Test
  void okShortEchoTag() {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_short_echo_tag.php");
  }

  @Test
  // SONARPHP-436
  void okJustHtml() {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok_just_html.php");
  }

  @Test
  void ko() {
    CheckVerifier.verify(check, TEST_DIR + "ko.php");
  }

  @ParameterizedTest
  @ValueSource(strings = {"1", "2", "3", "4"})
  void koWithStartingComment(String pathSuffix) {
    String path = TEST_DIR + "ko_starting_comment_" + pathSuffix + ".php";
    CheckVerifier.verify(check, path);
  }

}
