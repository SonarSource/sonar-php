/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;

class ExpressionComplexityCheckTest {

  private static final String FILE_NAME = "ExpressionComplexityCheck.php";
  private ExpressionComplexityCheck check = new ExpressionComplexityCheck();

  @Test
  void defaultValue() throws Exception {
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void custom() throws Exception {
    check.max = 5;
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), Collections.singletonList(new LineIssue(check, 11, null)));
  }
}
