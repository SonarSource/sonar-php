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

import org.junit.Test;
import org.sonar.php.PHPAstScanner;
import org.sonar.php.checks.FormattingStandardCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

public class CurlyBraceCheckTest extends FormattingStandardCheckTest {


  @Test
  public void defaultValue() throws Exception {
    activeOnly("isOpenCurlyBraceForClassAndFunction", "isOpenCurlyBraceForControlStructures", "isClosingCurlyNextToKeyword");

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

      .next().atLine(82).withMessage("Move this \"else\" to the same line as the previous closing curly brace.")
      .next().atLine(87)
      .next().atLine(89)

      .noMore();
  }

  @Test
  public void custom() throws Exception {
    deactivateAll();

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "CurlyBraceCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }
}
