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

public class IndentationCheckTest extends FormattingStandardCheckTest {

  private static final File TEST_FILE = TestUtils.getCheckFile(TEST_DIR + "IndentationCheck.php");

  @Test
  public void defaultValue() throws IllegalAccessException {
    activeOnly("isMethodArgumentsIndentation");

    SourceFile file = PHPAstScanner.scanSingleFile(TEST_FILE, check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(6).withMessage("Either split this list into multiple lines and aligned at column \"4\" or move it on the same line \"6\".")
      .next().atLine(11).withMessage("Either split this list into multiple lines and aligned at column \"4\" or move it on the same line \"10\".")
      .next().atLine(15).withMessage("Align all arguments in this list at column \"4\".")
      .next().atLine(16).withMessage("Move the closing parenthesis on the next line.")
      .next().atLine(18)

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
