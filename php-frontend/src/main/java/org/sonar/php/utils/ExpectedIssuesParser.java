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
package org.sonar.php.utils;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;

import org.sonar.plugins.php.api.tests.TestIssue;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class ExpectedIssuesParser extends PHPVisitorCheck {

  private final List<TestIssue> expectedIssues = new ArrayList<>();

  public static List<TestIssue> parseExpectedIssues(PhpFile file, CompilationUnitTree tree) {
    ExpectedIssuesParser expectedIssuesParser = new ExpectedIssuesParser();
    expectedIssuesParser.analyze(file, tree);

    return expectedIssuesParser.expectedIssues;
  }

  @Override
  public void visitTrivia(SyntaxTrivia syntaxTrivia) {
    String text = syntaxTrivia.text();
    int issueLine = syntaxTrivia.line();

    if (text.startsWith("// NOK")) {
      String message = null;

      int expectedMessageIndex = text.indexOf("{{");
      if (expectedMessageIndex > -1) {
        message = text.substring(expectedMessageIndex + 2, text.indexOf("}}"));
      }

      if (text.contains("@+")) {
        String[] spaceSplit = text.substring(text.indexOf("NOK") + 3).split("[\\s\\[{]", 2);
        issueLine += Integer.valueOf(spaceSplit[0].substring(2));
      }

      TestIssue issue = TestIssue.create(message,  issueLine);
      expectedIssues.add(issue);

      if (text.contains("[[")) {
        addParams(issue, text.substring(text.indexOf("[[") + 2, text.indexOf("]]")));
      }

    } else if (text.contains("^")) {
      addPreciseLocation(syntaxTrivia, text, issueLine);
    }
  }

  private void addPreciseLocation(SyntaxTrivia syntaxTrivia, String text, int issueLine) {
    if (syntaxTrivia.column() > 1) {
      throw new IllegalStateException("Line " + issueLine + ": comments asserting a precise location should start at column 1");
    }

    String missingAssertionMessage = String.format("Invalid test file: a precise location is provided at line %s but no issue is asserted at line %s", issueLine, issueLine - 1);
    if (expectedIssues.isEmpty()) {
      throw new IllegalStateException(missingAssertionMessage);
    }
    TestIssue issue = expectedIssues.get(expectedIssues.size() - 1);
    if (issue.line() != issueLine - 1) {
      throw new IllegalStateException(missingAssertionMessage);
    }

    issue.endLine(issue.line());
    issue.startColumn(text.indexOf('^') );
    issue.endColumn(text.lastIndexOf('^') + 1);
  }

  private static int lineValue(int baseLine, String shift) {
    if (shift.startsWith("+")) {
      return baseLine + Integer.valueOf(shift.substring(1));
    }
    if (shift.startsWith("-")) {
      return baseLine - Integer.valueOf(shift.substring(1));
    }
    return Integer.valueOf(shift);
  }

  private static void addParams(TestIssue issue, String params) {
    for (String param : Splitter.on(';').split(params)) {
      int equalIndex = getEqualIndex(param, issue);
      String name = param.substring(0, equalIndex);
      String value = param.substring(equalIndex + 1);

      if ("effortToFix".equalsIgnoreCase(name)) {
        issue.effortToFix(Integer.valueOf(value));

      } else if ("secondary".equalsIgnoreCase(name)) {
        List<Integer> secondaryLines = new ArrayList<>();
        if (!"".equals(value)) {
          for (String secondary : Splitter.on(',').split(value)) {
            secondaryLines.add(lineValue(issue.line(), secondary));
          }
        }
        issue.secondary(secondaryLines);

      } else {
        throw new IllegalStateException("Invalid param at line " + issue.line() + ": " + name);
      }
    }
  }

  private static int getEqualIndex(String param, TestIssue issue) {
    int equalIndex = param.indexOf('=');
    if (equalIndex == -1) {
      throw new IllegalStateException("Invalid param at line " + issue.line() + ": " + param);
    }
    return equalIndex;
  }

}
