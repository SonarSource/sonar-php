/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.php.FileTestUtils;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhpCheckTestTest {

  private int tmpFileId = 0;

  @TempDir
  public File tempFolder;

  private static final PHPCheck CHECK = new DummyCheck();

  @Test
  void testNoIssue() {
    PHPCheckTest.check(CHECK, createFile("<?php $a += 1; // No issue"));
  }

  @Test
  void testWithMessage() {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK {{message}}"));
  }

  @Test
  void testWithoutMessage() {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK"));
  }

  @Test
  void testErrorUnexpectedIssue() {
    PhpFile file = createFile("<?php $a = 1;");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [UNEXPECTED_ISSUE] at line 1 with a message: \"message\"");
  }

  @Test
  void testErrorNoIssue() {
    PhpFile file = createFile("<?php $a += 1; // NOK");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [NO_ISSUE] Expected but no issue on line 1.");
  }

  @Test
  void testErrorWrongMessage() {
    PhpFile file = createFile("<?php $a = 1; // NOK {{another message}}");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_MESSAGE] Issue at line 1 : \n"
        + "Expected message : another message\n"
        + "Actual message : message");
  }

  @Test
  void testWrongNumber() {
    PhpFile file = createFile("<?php $a += 1; // NOK");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [NO_ISSUE] Expected but no issue on line 1.");
  }

  @Test
  void testCheckPassingIssuesOverridesComment() {
    // The rule will raise an issue but by giving a empty list we say we expect no issue despite of the comment
    PhpFile file = createFile("<?php $a = 1; // NOK {{message}}");
    List<PhpIssue> issues = createIssuesForLines( /* None */);
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file, issues))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [UNEXPECTED_ISSUE] at line 1 with a message: \"message\"");
  }

  @Test
  void testMultipleIssueOnSameLine() {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; $a = 1; $a = 1;"), createIssuesForLines(1, 1, 1));
  }

  @Test
  void testMultipleIssueOnSameLineWrongMessage() {
    PhpFile file = createFile("<?php $a = 1; $a = 1; $a = 1;");
    List<PhpIssue> issues = createIssuesForLines("another message", 1, 1, 1);
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file, issues))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_MESSAGE] Issue at line 1 : \n"
        + "Expected message : another message\n"
        + "Actual message : message");
  }

  @Test
  void testErrorWrongNumber1() {
    PhpFile file = createFile("<?php $a = 1; $a = 1; // NOK");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_NUMBER] Line 1: Expecting 1 issue, but actual issues number is 2");
  }

  @Test
  void testErrorWrongNumber2() {
    PhpFile file = createFile("<?php $a = 1; // NOK");
    List<PhpIssue> issues = createIssuesForLines(1, 1);
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file, issues))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_NUMBER] Line 1: Expecting 2 issue, but actual issues number is 1");
  }

  @Test
  void testExpectedCost() {
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[effortToFix=2]]"));
  }

  @Test
  void testNoExpectedCost() {
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK"));
  }

  @Test
  void testWrongCost() {
    PHPCheck check = new DummyCheck(2);
    PhpFile file = createFile("<?php $a = 1; // NOK [[effortToFix=3]]");
    assertThatThrownBy(() -> PHPCheckTest.check(check, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_COST] Issue at line 1 : \n"
        + "Expected cost : 3.0\n"
        + "Actual cost : 2.0");
  }

  @Test
  void missingCost() {
    PhpFile file = createFile("<?php $a = 1; // NOK [[effortToFix=3]]");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_COST] Issue at line 1 : \n"
        + "Expected cost : 3.0\n"
        + "Actual cost : null");
  }

  @Test
  void testPreciseLocation() {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK\n" +
      "//    ^^^^^^      "));
  }

  @Test
  void testWrongStartPreciseLocation() {
    PhpFile file = createFile("<?php $a = 1; // NOK\n//   ^^^^^^^      ");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_PRIMARY_LOCATION] Line 1: actual start column is 6");
  }

  @Test
  void testWrongEndPreciseLocation() {
    PhpFile file = createFile("<?php $a = 1; // NOK\n//    ^^^^^       ");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_PRIMARY_LOCATION] Line 1: actual end column is 12");
  }

  @Test
  void testIssueWithoutPreciseLocation() {
    PhpFile file = createFile("<?php class A {} // NOK\n//    ^^^^^      ");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [NO_PRECISE_LOCATION] Line 1: issue with precise location is expected");
  }

  @Test
  void testSecondaryLocation() {
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK [[secondary=+0,-0]] {{message}}"));
    PHPCheckTest.check(CHECK, createFile("<?php $a = 1; // NOK [[secondary=1,1]] {{message}}"));
  }

  @Test
  void testMissingSecondaryLocation() {
    PhpFile file = createFile("<?php $a = 1; // NOK [[secondary=+1]] {{message}}");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_SECONDARY_LOCATION] Line 1: missing secondary location at line 2");
  }

  @Test
  void testUnexpectedSecondaryLocation() {
    PhpFile file = createFile("<?php $a = 1; // NOK [[secondary=+0]] {{message}}");
    assertThatThrownBy(() -> PHPCheckTest.check(CHECK, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_SECONDARY_LOCATION] Line 1: unexpected secondary location at line 1");
  }

  @Test
  void testWrongCostWithSecondary() {
    PHPCheck check = new DummyCheck(2);
    PhpFile file = createFile("<?php $a = 1; // NOK [[effortToFix=3;secondary=1,1]]");
    assertThatThrownBy(() -> PHPCheckTest.check(check, file))
      .isInstanceOf(AssertionError.class)
      .hasMessageContaining("* [WRONG_COST] Issue at line 1 : \n"
        + "Expected cost : 3.0\n"
        + "Actual cost : 2.0");
  }

  @Test
  void testShiftedLine() {
    PHPCheckTest.check(CHECK, createFile("<?php \n// NOK@+1\n$a = 1;"));
  }

  @Test
  void testSecondaryWithCost() {
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[effortToFix=2;secondary=1,1]]"));
    PHPCheckTest.check(new DummyCheck(2), createFile("<?php $a = 1; // NOK [[secondary=1,1;effortToFix=2]]"));
  }

  private List<PhpIssue> createIssuesForLines(int... lines) {
    return createIssuesForLines("message", lines);
  }

  private List<PhpIssue> createIssuesForLines(String message, int... lines) {
    List<PhpIssue> issues = new ArrayList<>();
    for (int line : lines) {
      issues.add(new LineIssue(CHECK, line, message));
    }
    return issues;
  }

  private PhpFile createFile(String content) {
    return FileTestUtils.getFile(new File(tempFolder, "test_check" + tmpFileId++ + ".php"), content);
  }

}
