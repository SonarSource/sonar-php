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
package org.sonar.php.checks;

import org.junit.Test;
import org.sonar.php.PHPAstScanner;
import org.sonar.plugins.php.CheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

public class UnusedLocalVariableCheckTest extends CheckTest {

  @Test
  public void test() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("UnusedLocalVariableCheck.php"), new UnusedLocalVariableCheck());

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(9).withMessage("Remove this unused \"$c\" local variable.")
      .next().atLine(10).withMessage("Remove this unused \"$d\" local variable.")
      .next().atLine(21)
      .next().atLine(31)
      .next().atLine(40)
      .next().atLine(56)
      .next().atLine(67)
      .next().atLine(67)
      .next().atLine(126)
      .next().atLine(128).withMessage("Remove this unused \"$c\" local variable.")
      //redefine tests
      .next().atLine(180).withMessage("Remove this unused \"$j\" local variable.")
      .next().atLine(183).withMessage("Remove this unused \"$a\" local variable.")
      .next().atLine(186).withMessage("Remove this unused \"$b\" local variable.")
      //foreach tests
      .next().atLine(211).withMessage("Remove this unused \"$v5\" local variable.")
      .next().atLine(215).withMessage("Remove this unused \"$v6\" local variable.")
      .next().atLine(218).withMessage("Remove this unused \"$v7\" local variable.")
      .next().atLine(221).withMessage("Remove this unused \"$v8\" local variable.")
      .next().atLine(225).withMessage("Remove this unused \"$k9\" local variable.")
      .next().atLine(231).withMessage("Remove this unused \"$v10\" local variable.")
      .next().atLine(235).withMessage("Remove this unused \"$k11\" local variable.")
      .next().atLine(239).withMessage("Remove this unused \"$v12\" local variable.")
      .next().atLine(243).withMessage("Remove this unused \"$k13\" local variable.")
      .next().atLine(248).withMessage("Remove this unused \"$k14\" local variable.")
      .next().atLine(265).withMessage("Remove this unused \"$c\" local variable.")
      .next().atLine(301).withMessage("Remove this unused \"$object2\" local variable.")
      .noMore();
  }
}
