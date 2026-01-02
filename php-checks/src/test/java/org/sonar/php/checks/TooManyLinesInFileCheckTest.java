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
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;

class TooManyLinesInFileCheckTest {

  private TooManyLinesInFileCheck check = new TooManyLinesInFileCheck();
  private String fileName = "TooManyLinesInFileCheck.php";

  @Test
  void defaultValue() throws Exception {
    CheckVerifier.verifyNoIssue(check, fileName);
  }

  @Test
  void custom() throws Exception {
    check.max = 2;
    PHPCheckTest.check(check, TestUtils.getCheckFile(fileName), Collections.singletonList(new LineIssue(
      check,
      0,
      "File \"TooManyLinesInFileCheck.php\" has 3 lines, which is greater than " + check.max + " authorized. Split it into smaller files.")));
  }
}
