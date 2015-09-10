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

public class GenericExceptionCheckTest extends CheckTest {

  private GenericExceptionCheck check = new GenericExceptionCheck();
  private static final String TEST_DIR = "GenericExceptionCheck/";

  @Test
  public void ok_non_namespace() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ok1.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void ok_namespace() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ok2.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

  @Test
  public void ko_non_namespace() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko1.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(5).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(8).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(11).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(14).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(17).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(20).withMessage(GenericExceptionCheck.MESSAGE)
      .noMore();
  }

  @Test
  public void ko_namespace() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko2.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(7).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(10).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(13).withMessage(GenericExceptionCheck.MESSAGE)
      .noMore();
  }

  @Test
  public void ko_namespace_use() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko3.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(14).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(17).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(20).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(23).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(26).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(29).withMessage(GenericExceptionCheck.MESSAGE)
      .noMore();
  }

  @Test
  public void ko_multiple_namespaces() throws Exception {
    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile(TEST_DIR + "ko4.php"), check);

    checkMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(8).withMessage(GenericExceptionCheck.MESSAGE)
      .next().atLine(19).withMessage(GenericExceptionCheck.MESSAGE)
      .noMore();
  }

}
