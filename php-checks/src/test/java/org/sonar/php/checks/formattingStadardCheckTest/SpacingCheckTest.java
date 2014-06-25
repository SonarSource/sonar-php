/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks.formattingStadardCheckTest;

import org.junit.Test;
import org.sonar.php.PHPAstScanner;
import org.sonar.php.checks.FormattingStandardCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

public class SpacingCheckTest extends FormattingStandardCheckTest {


  @Test
  public void defaultValue() throws IllegalAccessException {
    activeOnly("isOneSpaceBetweenRParentAndLCurly", "isOneSpaceAfterComma", "isNoSpaceAfterMethodName", "isNoSpaceParenthesis");

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "SpacingCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(6).withMessage("Put one space between the closing parenthesis and the opening curly brace.")
      .next().atLine(8).withMessage("Put only one space between the closing parenthesis and the opening curly brace.")

      .next().atLine(24).withMessage("Remove any space before comma separated arguments.")
      .next().atLine(25).withMessage("Put exactly one space after comma separated arguments.")
      .next().atLine(26).withMessage("Remove any space before comma separated arguments and put exactly one space after comma separated arguments.")
      .next().atLine(27).withMessage("Remove any space before comma separated arguments and put exactly one space after comma separated arguments.")
      .next().atLine(28)

      .next().atLine(35).withMessage("Remove all space between the method name \"f\" and the opening parenthesis.")
      .next().atLine(36).withMessage("Remove all space between the method name \"doSomething\" and the opening parenthesis.")

      .next().atLine(44).withMessage("Remove all space after the opening parenthesis.")
      .next().atLine(45).withMessage("Remove all space before the closing parenthesis.")
      .next().atLine(46).withMessage("Remove all space after the opening parenthesis and before the closing parenthesis.")

      .noMore();
  }

  @Test
  public void custom() throws IllegalAccessException {
    deactivateAll();

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "SpacingCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }
}
