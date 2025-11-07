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

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.FileIssue;

class MoreThanOneClassInFileCheckTest {

  private static final String TEST_DIR = "MoreThanOneClassInFileCheck/";
  private final MoreThanOneClassInFileCheck check = new MoreThanOneClassInFileCheck();

  @Test
  void ok() throws Exception {
    CheckVerifier.verifyNoIssue(check, TEST_DIR + "ok.php");
  }

  @Test
  void ko1() throws Exception {
    String message = "There are 2 independent classes in this file; move all but one of them to other files.";
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko1.php"), Collections.singletonList(new FileIssue(check, message).cost(1.0)));
  }

  @Test
  void ko2() throws Exception {
    String message = "There are 2 independent interfaces in this file; move all but one of them to other files.";
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko2.php"), Collections.singletonList(new FileIssue(check, message).cost(1.0)));
  }

  @Test
  void ko3() throws Exception {
    String message = "There are 1 independent classes and 2 independent interfaces in this file; move all but one of them to other files.";
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_DIR + "ko3.php"), Collections.singletonList(new FileIssue(check, message).cost(2.0)));
  }

}
