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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.LineIssue;

class FieldNameCheckTest {

  private static final String FILE_NAME = "FieldNameCheck.php";

  private FieldNameCheck check = new FieldNameCheck();

  @Test
  void defaultValue() throws Exception {
    CheckVerifier.verify(check, FILE_NAME);
  }

  @Test
  void custom() throws Exception {
    check.format = "^[A-Z][a-zA-Z0-9]*$";
    String format = check.format;
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_NAME), List.of(
      new LineIssue(check, 7, "Rename this field \"$myVariable\" to match the regular expression " + format + "."),
      new LineIssue(check, 12, "Rename this field \"$myField\" to match the regular expression " + format + "."),
      new LineIssue(check, 15, "Rename this field \"$my_field\" to match the regular expression " + format + "."),
      new LineIssue(check, 16, "Rename this field \"$myReadonly\" to match the regular expression " + format + "."),
      new LineIssue(check, 17, "Rename this field \"$myFinalField\" to match the regular expression " + format + "."),
      new LineIssue(check, 18, "Rename this field \"$MY_FINAL_FIELD\" to match the regular expression " + format + ".")));
  }
}
