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
package org.sonar.php.checks;

import org.junit.Test;
import org.sonar.php.PHPAstScanner;
import org.sonar.plugins.php.CheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

public class ForLoopCounterChangedCheckTest extends CheckTest {

  @Test
  public void test() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("ForLoopCounterChangedCheck.php"), new ForLoopCounterChangedCheck());

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(4).withMessage("Refactor the code to avoid updating the loop counter \"$a\" within the loop body.")
      .next().atLine(8)
      .next().atLine(12)
      .next().atLine(13)
      .next().atLine(18)
      .next().atLine(21)
      .next().atLine(22)
      .next().atLine(24)
      .next().atLine(31)
      .next().atLine(32)
      .next().atLine(42)
      .next().atLine(43)
      .next().atLine(44)
      .next().atLine(45)
      .next().atLine(49)
      .next().atLine(58)
      .next().atLine(60)
      .next().atLine(66)
      .next().atLine(79)
      .next().atLine(87)
      .noMore();
  }
}
