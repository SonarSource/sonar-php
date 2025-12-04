/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.PhpIssue;

class FileHeaderCheckTest {

  private static final String FILE_2 = "FileHeaderCheck/file2.php";
  private FileHeaderCheck check = new FileHeaderCheck();

  @Test
  void test() throws Exception {
    List<PhpIssue> issue = Collections.singletonList(new FileIssue(check, "Add or update the header of this file."));
    List<PhpIssue> noIssue = Collections.emptyList();

    check.headerFormat = "// copyright 2005";
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/file1.php"), noIssue);
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), issue);
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/file4.php"), noIssue);

    check.headerFormat = "// copyright 20\\d\\d";
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/file1.php"), issue);

    check.headerFormat = "// copyright 2012";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), noIssue);

    check.headerFormat = "// copyright 2012\n// foo";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), noIssue);

    check.headerFormat = "// copyright 2012\r\n// foo";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), noIssue);

    check.headerFormat = "// copyright 2012\r// foo";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), noIssue);

    check.headerFormat = "// copyright 2012\r\r// foo";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), issue);

    check.headerFormat = "// copyright 2012\n// foo\n\n\n\n\n\n\n\n\n\ngfoo";
    PHPCheckTest.check(check, TestUtils.getCheckFile(FILE_2), issue);

    check.headerFormat = "/*foo http://www.example.org*/";
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/file3.php"), noIssue);

    check.headerFormat = "// copyright 2012\n// foo";
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/file5.php"), issue);

    check = new FileHeaderCheck();
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/empty.php"), noIssue);
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/emptyWithTag.php"), noIssue);
    PHPCheckTest.check(check, TestUtils.getCheckFile("FileHeaderCheck/single_line_break.php"), noIssue);
  }

}
