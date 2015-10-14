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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.php.tree.visitors.PHPIssue;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tests.PHPCheckTest;
import org.sonar.plugins.php.api.visitors.Issue;

import java.util.List;

public class MissingNewLineAtEOFCheckTest {

  private MissingNewLineAtEOFCheck check = new MissingNewLineAtEOFCheck();
  private static final String TEST_FILE_DIR = "MissingNewLineAtEOF/";
  private List<Issue> issue = ImmutableList.<Issue>of(new PHPIssue("testKey", "Add a new line at the end of this file."));

  @Test
  public void noNewLine() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_FILE_DIR + "MissingNewLineAtEOF.php"), issue);
  }

  @Test
  public void emptyFile() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_FILE_DIR + "EmptyFile.php"), issue);
  }

  @Test
  public void newLine() throws Exception {
    PHPCheckTest.check(check, TestUtils.getCheckFile(TEST_FILE_DIR + "NewLineAtEOF.php"));
  }

}
