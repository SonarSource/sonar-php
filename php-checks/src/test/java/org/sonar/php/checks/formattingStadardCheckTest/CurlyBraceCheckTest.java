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

public class CurlyBraceCheckTest extends FormattingStandardCheckTest {


  @Test
  public void defaultValue() {
    check.hasNamespaceBlankLine = false;
    check.isUseAfterNamespace = false;
    check.hasUseBlankLine = false;
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "CurlyBraceCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(7).withMessage("Move this open curly brace to the beginning of the next line.")
      .next().atLine(8)
      .next().atLine(20)
      .next().atLine(23)
      .next().atLine(26)

      .next().atLine(39).withMessage("Move this open curly brace to the end of the previous line.")
      .next().atLine(43)
      .next().atLine(48)
      .next().atLine(52)
      .next().atLine(56)
      .next().atLine(60)
      .next().atLine(64)
      .next().atLine(66)
      .noMore();
  }

  @Test
  public void custom() {
    check.isOpenCurlyBraceForClassAndFunction = false;
    check.isOpenCurlyBraceForControlStructures = false;

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "CurlyBraceCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }
}
