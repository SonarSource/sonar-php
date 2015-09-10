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

public class StringLiteralDuplicatedCheckTest extends CheckTest {

  private StringLiteralDuplicatedCheck check = new StringLiteralDuplicatedCheck();

  @Test
  public void defaultValue() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("StringLiteralDuplicatedCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(10).withMessage("Define a constant instead of duplicating this literal \"aaaaa\" 3 times.")
      .next().atLine(14).withMessage("Define a constant instead of duplicating this literal \"$toto\" 3 times.")
      .next().atLine(18).withMessage("Define a constant instead of duplicating this literal \"name1\" 4 times.")
      .noMore();
  }

  @Test
  public void custom() throws Exception {
    check.threshold = 4;

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("StringLiteralDuplicatedCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(18)
      .noMore();
  }
}
