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
package org.sonar.plugins.php.api.tests;

import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.FileTestUtils;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhpCheckTestTest {

  private int tmpFileId = 0;

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final PHPCheck CHECK = new DummyCheck();

  @Test
  public void test_no_issue() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php $a += 1; // No issue"));
  }

  @Test
  public void test_with_message() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK {{message}}"));
  }

  @Test
  public void test_without_message() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK"));
  }

  @Test
  public void test_error_unexpected_issue() throws Exception {
    PhpFile file = createFile("<?php $a = 1;");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [UNEXPECTED_ISSUE] at line 1 with a message: \"message\"");
  }

  @Test
  public void test_error_no_issue() throws Exception {
    PhpFile file = createFile("<?php $a += 1; // NOK");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [NO_ISSUE] Expected but no issue on line 1.");
  }

  @Test
  public void test_error_wrong_message() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK {{another message}}");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_MESSAGE] Issue at line 1 : \n"
        + "Expected message : another message\n"
        + "Actual message : message");
  }

  @Test
  public void test_wrong_number() throws Exception {
    PhpFile file = createFile("<?php $a += 1; // NOK");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [NO_ISSUE] Expected but no issue on line 1.");
  }

  @Test
  public void test_check_passing_issues_overrides_comment() throws Exception {
    // The rule will raise an issue but by giving a empty list we say we expect no issue despite of the comment
    PhpFile file = createFile("<?php $a = 1; // NOK {{message}}");
    List<PhpIssue> issues = createIssuesForLines( /* None */);
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file, issues))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [UNEXPECTED_ISSUE] at line 1 with a message: \"message\"");
  }

  @Test
  public void test_multiple_issue_on_same_line() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; $a = 1; $a = 1;"), createIssuesForLines(1, 1, 1));
  }

  @Test
  public void test_multiple_issue_on_same_line_wrong_message() throws Exception {
    PhpFile file = createFile("<?php $a = 1; $a = 1; $a = 1;");
    List<PhpIssue> issues = createIssuesForLines("another message", 1, 1, 1);
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file, issues))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_MESSAGE] Issue at line 1 : \n"
        + "Expected message : another message\n"
        + "Actual message : message");
  }

  @Test
  public void test_error_wrong_number1() throws Exception {
    PhpFile file = createFile("<?php $a = 1; $a = 1; // NOK");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_NUMBER] Line 1: Expecting 1 issue, but actual issues number is 2");
  }

  @Test
  public void test_error_wrong_number2() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK");
    List<PhpIssue> issues = createIssuesForLines(1, 1);
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file, issues))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_NUMBER] Line 1: Expecting 2 issue, but actual issues number is 1");
  }

  @Test
  public void test_expected_cost() throws Exception {
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[effortToFix=2]]"));
  }

  @Test
  public void test_no_expected_cost() throws Exception {
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK"));
  }

  @Test
  public void test_wrong_cost() throws Exception {
    PHPCheck check = new DummyCheck(2);
    PhpFile file = createFile("<?php $a = 1; // NOK [[effortToFix=3]]");
    assertThatThrownBy(() ->  PHPCheckTest.check(check, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_COST] Issue at line 1 : \n"
        + "Expected cost : 3.0\n"
        + "Actual cost : 2.0");
  }

  @Test
  public void missing_cost() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK [[effortToFix=3]]");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_COST] Issue at line 1 : \n"
        + "Expected cost : 3.0\n"
        + "Actual cost : null");
  }

  @Test
  public void test_precise_location() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK\n" +
      "//    ^^^^^^      "));
  }

  @Test
  public void test_wrong_start_precise_location() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK\n//   ^^^^^^^      ");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_PRIMARY_LOCATION] Line 1: actual start column is 6");
  }

  @Test
  public void test_wrong_end_precise_location() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK\n//    ^^^^^       ");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_PRIMARY_LOCATION] Line 1: actual end column is 12");
  }

  @Test
  public void test_issue_without_precise_location() throws Exception {
    PhpFile file = createFile("<?php class A {} // NOK\n//    ^^^^^      ");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [NO_PRECISE_LOCATION] Line 1: issue with precise location is expected");
  }

  @Test
  public void test_secondary_location() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK [[secondary=+0,-0]] {{message}}"));
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK [[secondary=1,1]] {{message}}"));
  }

  @Test
  public void test_missing_secondary_location() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK [[secondary=+1]] {{message}}");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_SECONDARY_LOCATION] Line 1: missing secondary location at line 2");
  }

  @Test
  public void test_unexpected_secondary_location() throws Exception {
    PhpFile file = createFile("<?php $a = 1; // NOK [[secondary=+0]] {{message}}");
    assertThatThrownBy(() ->  PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_SECONDARY_LOCATION] Line 1: unexpected secondary location at line 1");
  }

  @Test
  public void test_wrong_cost_with_secondary() throws Exception {
    PHPCheck check = new DummyCheck(2);
    PhpFile file = createFile("<?php $a = 1; // NOK [[effortToFix=3;secondary=1,1]]");
    assertThatThrownBy(() ->  PHPCheckTest.check(check, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_COST] Issue at line 1 : \n"
        + "Expected cost : 3.0\n"
        + "Actual cost : 2.0");
  }

  @Test
  public void test_shifted_line() throws Exception {
    PHPCheckTest.check(CHECK, createFile("<?php \n// NOK@+1\n$a = 1;"));
  }

  @Test
  public void test_secondary_with_cost() throws Exception {
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[effortToFix=2;secondary=1,1]]"));
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[secondary=1,1;effortToFix=2]]"));
  }

  private List<PhpIssue> createIssuesForLines(int... lines) {
    return createIssuesForLines("message", lines);
  }

  private List<PhpIssue> createIssuesForLines(String message, int... lines) {
    List<PhpIssue> issues = new ArrayList<>();
    for (int line : lines) {
      issues.add(new LegacyIssue(CHECK, message).line(line));
    }
    return issues;
  }

  private PhpFile createFile(String content) throws Exception {
    return FileTestUtils.getFile(tmpFolder.newFile("test_check" + tmpFileId++ +  ".php"), content);
  }

}
