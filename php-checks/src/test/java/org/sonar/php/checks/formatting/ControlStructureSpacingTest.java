/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.checks.formatting;

import org.junit.Before;
import org.junit.Test;
import org.sonar.php.PHPAstScanner;
import org.sonar.php.checks.FormattingStandardCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;

public class ControlStructureSpacingTest extends FormattingStandardCheckTest {

  private static File TEST_FILE;

  @Before
  public void setUp() throws Exception {
    TEST_FILE = TestUtils.getCheckFile(TEST_DIR + "ControlStructureSpacingCheck.php");
  }

  @Test
  public void defaultValue() throws IllegalAccessException {
    activeOnly("isOneSpaceBetweenKeywordAndNextToken", "isOneSpaceAfterForLoopSemicolon", "isSpaceForeachStatement");

    SourceFile file = PHPAstScanner.scanSingleFile(TEST_FILE, check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(6).withMessage("Put one space between this \"if\" keyword and the opening parenthesis.")
      .next().atLine(8).withMessage("Put only one space between this \"if\" keyword and the opening parenthesis.")
      .next().atLine(10).withMessage("Put one space between this \"else\" keyword and the opening curly brace.")

      .next().atLine(29).withMessage("Put exactly one space after each \";\" character in the \"for\" statement.")

      .next().atLine(39).withMessage("Put exactly one space after and before \"as\" in \"foreach\" statement.")
      .next().atLine(40).withMessage("Put exactly one space after and before \"=>\" in \"foreach\" statement.")
      .next().atLine(41).withMessage("Put exactly one space after and before \"as\" and \"=>\" in \"foreach\" statement.")

      .noMore();
  }

  @Test
  public void custom() throws IllegalAccessException {
    deactivateAll();

    SourceFile file = PHPAstScanner.scanSingleFile(TEST_FILE, check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }
}
