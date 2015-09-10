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

public class MoreThanOneClassInFileCheckTest extends CheckTest {

  private static final String TEST_DIR = "MoreThanOneClassInFileCheck/";
  private final MoreThanOneClassInFileCheck check = new MoreThanOneClassInFileCheck();

  @Test
  public void ok() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ok.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void ko1() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko1.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().withCost(1.0).atLine(null).withMessage("There are 2 independent classes in this file; move all but one of them to other files.")
      .noMore();
  }

  @Test
  public void ko2() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko2.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null).withCost(1.0).withMessage("There are 2 independent interfaces in this file; move all but one of them to other files.")
      .noMore();
  }

  @Test
  public void ko3() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko3.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null).withCost(2.0).withMessage("There are 1 independent classes and 2 independent interfaces in this file; move all but one of them to other files.")
      .noMore();
  }

}
