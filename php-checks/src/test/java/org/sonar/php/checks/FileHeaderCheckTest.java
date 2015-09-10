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
import org.sonar.squidbridge.checks.CheckMessagesVerifier;

public class FileHeaderCheckTest extends CheckTest {

  private FileHeaderCheck check = new FileHeaderCheck();

  @Test
  public void test() throws Exception {
    check.headerFormat = "// copyright 2005";

    SourceFile file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file1.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file4.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 20\\d\\d";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file1.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2005";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null).withMessage("Add or update the header of this file.");

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\n// foo";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r// foo";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\r\r// foo";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "// copyright 2012\n// foo\n\n\n\n\n\n\n\n\n\ngfoo";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file2.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .next().atLine(null);

    check = new FileHeaderCheck();
    check.headerFormat = "/*foo http://www.example.org*/";

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/file3.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();

    check = new FileHeaderCheck();

    file = PHPAstScanner.scanSingleFile(TestUtils.getCheckFile("FileHeaderCheck/empty.php"), check);
    CheckMessagesVerifier.verify(file.getCheckMessages())
      .noMore();
  }

}
