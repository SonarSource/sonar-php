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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class TabCharacterCheckTest {

  @Test
  void test() throws Exception {
    PHPCheck check = new TabCharacterCheck();

    List<PhpIssue> issue = Collections.singletonList(new FileIssue(check, "Replace all tab characters in this file by sequences of white-spaces."));
    PHPCheckTest.check(check, TestUtils.getCheckFile("TabCharacterCheck/TabCharacterCheck.php"), issue);
  }

  @Test
  void testOk() throws Exception {
    PHPCheck check = new TabCharacterCheck();
    CheckVerifier.verifyNoIssueIgnoringExpected(check, "TabCharacterCheck/TabCharacterCheck_ok.php");
  }
}
