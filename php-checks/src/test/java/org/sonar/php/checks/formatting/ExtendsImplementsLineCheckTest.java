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
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class ExtendsImplementsLineCheckTest extends FormattingStandardCheckTest {

  @Test
  public void test() throws Exception {
    activeOnly("isExtendsAndImplementsLine");

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ExtendsImplementsLineCheck.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(3).withMessage("Move \"extends\" keyword to the same line as the declaration of its class name, \"KO\".")
      .next().atLine(9).withMessage("Move \"implements\" keyword to the same line as the declaration of its class name, \"KO\".")
      .next().atLine(15).withMessage("Move \"extends\" and \"implements\" keywords to the same line as the declaration of its class name, \"KO\".");
  }

  @Test
  public void custom() throws Exception {
    deactivateAll();

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ExtendsImplementsLineCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }
}
