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

public class MissingMethodVisibilityCheckTest extends CheckTest {

  @Test
  public void test() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("MissingMethodVisibilityCheck.php"), new MissingMethodVisibilityCheck());

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(5).withMessage("Explicitly mention the visibility of this method \"f\".")
      .next().atLine(6).withMessage("Explicitly mention the visibility of this method \"g\".")
      .next().atLine(7).withMessage("Explicitly mention the visibility of this method \"h\".")
      .next().atLine(16).withMessage("Explicitly mention the visibility of this constructor \"C2\".")
      .next().atLine(17).withMessage("Explicitly mention the visibility of this destructor \"__destruct\".")
      .next().atLine(21).withMessage("Explicitly mention the visibility of this constructor \"__construct\".")
      .noMore();
  }
}
