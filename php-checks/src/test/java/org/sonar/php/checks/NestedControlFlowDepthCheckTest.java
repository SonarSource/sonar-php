/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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
import org.sonar.squid.api.SourceFile;

public class NestedControlFlowDepthCheckTest extends CheckTest {

  private NestedControlFlowDepthCheck check = new NestedControlFlowDepthCheck();

  @Test
  public void defaultValue() {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("NestedControlFlowDepthCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(16).withMessage("Refactor this code to not nest more than " + check.DEFAULT + " \"if\", \"for\", \"while\", \"switch\" and \"try\" statements.")
      .next().atLine(19)
      .next().atLine(22)
      .next().atLine(25)
      .next().atLine(28)
      .next().atLine(30)
      .noMore();
  }

  @Test
  public void custom() {
    check.max = 4;

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("NestedControlFlowDepthCheck.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(30).withMessage("Refactor this code to not nest more than " + check.max + " \"if\", \"for\", \"while\", \"switch\" and \"try\" statements.")
      .noMore();
  }
}
