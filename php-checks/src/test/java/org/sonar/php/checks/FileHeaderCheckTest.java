/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.PhpIssue;

public class FileHeaderCheckTest {

  private static final String FILE_2 = "FileHeaderCheck/file2.php";
  private FileHeaderCheck check = new FileHeaderCheck();

  @Test
  public void test() throws Exception {
    List<PhpIssue> issue = Collections.singletonList(new LegacyIssue(check, "Add or update the header of this file."));
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
