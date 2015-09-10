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

public class OpeningPHPTagCheckTest extends CheckTest {

  private OpeningPHPTagCheck check = new OpeningPHPTagCheck();
  private final String TEST_DIR = "OpeningPHPTagCheck/";

  @Test
  public void ok_long_tag() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ok_long_tag.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void ok_short_echo_tag() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ok_short_echo_tag.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  // SONARPHP-436
  public void ok_just_html() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ok_just_html.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void ko() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko.php"), check);
    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(3).withMessage("Change this opening tag to either \"<?php\" or \"<?=\".")
      .noMore();

  }
}
