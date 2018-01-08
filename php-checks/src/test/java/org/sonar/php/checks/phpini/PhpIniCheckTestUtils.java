/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks.phpini;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.php.ini.BasePhpIniIssue;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.PhpIniParser;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;

public class PhpIniCheckTestUtils {

  private static final Pattern LINE_END_COMMENT = Pattern.compile(".*;\\s*Noncompliant(?:\\s*\\{\\{(.*)\\}\\})?.*");

  public static void check(PhpIniCheck check, File file) {
    PhpFile inputFile = TestUtils.getFile(file);
    PhpIniFile phpIniFile = new PhpIniParser().parse(inputFile);
    List<PhpIniIssue> actualIssues = check.analyze(phpIniFile);
    List<PhpIniIssue> expectedIssues = expectedIssues(file);
    compare(actualIssues, expectedIssues);
  }

  private static List<PhpIniIssue> expectedIssues(File file) {
    List<String> lines;
    try {
      lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read " + file);
    }

    List<PhpIniIssue> issues = new ArrayList<>();
    int lineNumber = 1;
    for (String line : lines) {
      Matcher matcher = LINE_END_COMMENT.matcher(line);
      if (matcher.matches()) {
        String expectedMessage = null;
        if (matcher.groupCount() > 0) {
          expectedMessage = matcher.group(1);
        }
        issues.add(new BasePhpIniIssue(expectedMessage).line(lineNumber));
      }
      lineNumber++;
    }
    return issues;
  }

  public static void check(PhpIniCheck check, File file, List<PhpIniIssue> expectedIssues) {
    PhpFile inputFile = TestUtils.getFile(file);
    PhpIniFile phpIniFile = new PhpIniParser().parse(inputFile);
    List<PhpIniIssue> actualIssues = check.analyze(phpIniFile);
    compare(actualIssues, expectedIssues);
  }

  private static void compare(List<PhpIniIssue> actualIssues, List<PhpIniIssue> expectedIssues) {
    Ordering<PhpIniIssue> issueOrdering = Ordering.from((i1, i2) -> Ints.compare(i1.line(), i2.line()));
    Iterator<PhpIniIssue> sortedActualIssues = issueOrdering.sortedCopy(actualIssues).iterator();
    Iterator<PhpIniIssue> sortedExpectedIssues = issueOrdering.sortedCopy(expectedIssues).iterator();
    while (sortedActualIssues.hasNext() && sortedExpectedIssues.hasNext()) {
      PhpIniIssue actual = sortedActualIssues.next();
      PhpIniIssue expected = sortedExpectedIssues.next();
      assertThat(actual.line()).isEqualTo(expected.line());
      if (expected.message() != null) {
        assertThat(actual.message()).isEqualTo(expected.message());
      }
    }
    if (sortedActualIssues.hasNext()) {
      PhpIniIssue actual = sortedActualIssues.next();
      throw new AssertionError("Unexpected issue at line " + actual.line() + ": " + actual.message());
    }
    if (sortedExpectedIssues.hasNext()) {
      PhpIniIssue expected = sortedExpectedIssues.next();
      throw new AssertionError("Missing issue at line " + expected.line());
    }
  }

  public static PhpIniIssue issue(int line) {
    return issue(line, null);
  }

  public static PhpIniIssue issue(int line, String message) {
    return issue(message).line(line);
  }

  public static PhpIniIssue issue(String message) {
    return new BasePhpIniIssue(message);
  }

}
