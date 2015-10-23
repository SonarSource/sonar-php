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
package org.sonar.plugins.php.api.tests;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.tree.visitors.PHPIssue;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.Issue;

import java.io.File;

public class PHPCheckTestTest extends PHPCheckTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test_no_issue() throws Exception {
    check(new DummyCheck(), createFile("<?php $a += 1; // No issue"));
  }

  @Test
  public void test_with_message() throws Exception {
    check(new DummyCheck(), createFile("<?php $a = 1; // NOK {{message}}"));
  }

  @Test
  public void test_without_message() throws Exception {
    check(new DummyCheck(), createFile("<?php $a = 1; // NOK"));
  }


  @Test
  public void test_error_unexpected_issue() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [UNEXPECTED_ISSUE] at line 1 with a message: \"message\"");

    check(new DummyCheck(), createFile("<?php $a = 1;"));
  }

  @Test
  public void test_error_no_issue() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [NO_ISSUE] Expected but no issue on line 1.");

    check(new DummyCheck(), createFile("<?php $a += 1; // NOK"));
  }

  @Test
  public void test_error_wrong_message() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [WRONG_MESSAGE] Issue at line 1 : \n"
      + "Expected message : another message\n"
      + "Actual message : message");

    check(new DummyCheck(), createFile("<?php $a = 1; // NOK {{another message}}"));
  }

  @Test
  public void test_wrong_number() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [NO_ISSUE] Expected but no issue on line 1.");

    check(new DummyCheck(), createFile("<?php $a += 1; // NOK"));
  }

  @Test
  public void test_check_passing_issues_overrides_comment() throws Exception {
    // The rule will raise an issue but by giving a empty list we say we expect no issue despite of the comment
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [UNEXPECTED_ISSUE] at line 1 with a message: \"message\"");

    check(new DummyCheck(), createFile("<?php $a = 1; // NOK {{message}}"), createIssuesForLines( /*None */));
  }

  @Test
  public void test_multiple_issue_on_same_line() throws Exception {
    check(new DummyCheck(), createFile("<?php $a = 1; $a = 1; $a = 1;"), createIssuesForLines(1, 1, 1));
  }

  @Test
  public void test_multiple_issue_on_same_line_wrong_message() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [WRONG_MESSAGE] Issue at line 1 : \n"
      + "Expected message : another message\n"
      + "Actual message : message");

    check(new DummyCheck(), createFile("<?php $a = 1; $a = 1; $a = 1;"), createIssuesForLines("another message", 1, 1, 1));
  }

  @Test
  public void test_error_wrong_number1() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [WRONG_NUMBER] Line 1: Expecting 1 issue, but actual issues number is 2");

    check(new DummyCheck(), createFile("<?php $a = 1; $a = 1; // NOK"));
  }

  @Test
  public void test_error_wrong_number2() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [WRONG_NUMBER] Line 1: Expecting 2 issue, but actual issues number is 1");

    check(new DummyCheck(), createFile("<?php $a = 1; // NOK"), createIssuesForLines(1, 1));
  }

  @Test
  public void test_expected_cost() throws Exception {
    check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[effortToFix=2]]"));
  }

  @Test
  public void test_no_expected_cost() throws Exception {
    check(new DummyCheck(2), createFile("<?php $a = 1; // NOK"));
  }

  @Test
  public void test_wrong_cost() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [WRONG_COST] Issue at line 1 : \n"
      + "Expected cost : 3.0\n"
      + "Actual cost : 2.0");

    check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[effortToFix=3]]"));
  }

  @Test
  public void missing_cost() throws Exception {
    thrown.expect(AssertionError.class);
    thrown.expectMessage("* [WRONG_COST] Issue at line 1 : \n"
      + "Expected cost : 3.0\n"
      + "Actual cost : null");

    check(new DummyCheck(), createFile("<?php $a = 1; // NOK [[effortToFix=3]]"));
  }

  public ImmutableList<Issue> createIssuesForLines(int... lines) {
    return createIssuesForLines("message", lines);
  }

  public ImmutableList<Issue> createIssuesForLines(String message, int... lines) {
    ImmutableList.Builder<Issue> issueBuilder = ImmutableList.builder();

    for (int line : lines) {
      issueBuilder.add(new PHPIssue("testKey", message).line(line));
    }

    return issueBuilder.build();
  }

  private File createFile(String content) throws Exception {
    File f = tmpFolder.newFile("test_check.php");
    FileUtils.writeStringToFile(f, content);

    return f;
  }

}

