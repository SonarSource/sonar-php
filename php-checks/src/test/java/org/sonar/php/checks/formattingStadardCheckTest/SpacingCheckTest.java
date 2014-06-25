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
    activeOnly("isOneSpaceBetweenRParentAndLCurly", "isOneSpaceBetweenKeywordAndNextToken",
      "isOneSpaceAfterForLoopSemicolon", "isOneSpaceAfterComma", "isNoSpaceAfterMethodName", "isSpaceForeachStatement");

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "SpacingCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(6).withMessage("Put one space between the closing parenthesis and the opening curly brace.")
      .next().atLine(8).withMessage("Put only one space between the closing parenthesis and the opening curly brace.")

      .next().atLine(23).withMessage("Put one space between this \"if\" keyword and the opening parenthesis.")
      .next().atLine(25).withMessage("Put only one space between this \"if\" keyword and the opening parenthesis.")
      .next().atLine(27).withMessage("Put one space between this \"else\" keyword and the opening curly brace.")

      .next().atLine(46).withMessage("Put exactly one space after each \";\" character in the \"for\" statement.")

      .next().atLine(56).withMessage("Remove any space before comma separated arguments.")
      .next().atLine(57).withMessage("Put exactly one space after comma separated arguments.")
      .next().atLine(58).withMessage("Remove any space before comma separated arguments and put exactly one space after comma separated arguments.")
      .next().atLine(59).withMessage("Remove any space before comma separated arguments and put exactly one space after comma separated arguments.")
      .next().atLine(60)

      .next().atLine(67).withMessage("Remove all space between the method name \"f\" and the opening parenthesis.")
      .next().atLine(68).withMessage("Remove all space between the method name \"doSomething\" and the opening parenthesis.")

      .next().atLine(76).withMessage("Put exactly one space after and before \"as\" in \"foreach\" statement.")
      .next().atLine(77).withMessage("Put exactly one space after and before \"=>\" in \"foreach\" statement.")
      .next().atLine(78).withMessage("Put exactly one space after and before \"as\" and \"=>\" in \"foreach\" statement.")

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
