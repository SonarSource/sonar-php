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

import java.io.File;

public class FunctionSpacingCheckTest extends FormattingStandardCheckTest {

  private static final File TEST_FILE = TestUtils.getCheckFile(TEST_DIR + "FunctionSpacingCheck.php");

  @Test
  public void defaultValue() throws IllegalAccessException {
    activeOnly("isOneSpaceAfterComma", "isNoSpaceAfterMethodName");

    SourceFile file = PHPAstScanner.scanSingleFile(TEST_FILE, check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(7).withMessage("Remove any space before comma separated arguments.")
      .next().atLine(8).withMessage("Put exactly one space after comma separated arguments.")
      .next().atLine(9).withMessage("Remove any space before comma separated arguments and put exactly one space after comma separated arguments.")
      .next().atLine(10).withMessage("Remove any space before comma separated arguments and put exactly one space after comma separated arguments.")
      .next().atLine(11)

      .next().atLine(18).withMessage("Remove all space between the method name \"f\" and the opening parenthesis.")
      .next().atLine(19).withMessage("Remove all space between the method name \"doSomething\" and the opening parenthesis.")

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
