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

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.apache.commons.lang.StringUtils;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.Issue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PHPCheckTest {

  private static final ActionParser<Tree> parser = PHPParserBuilder.createParser(Charsets.UTF_8);

  public static void check(PHPCheck check, File file) {
    CompilationUnitTree tree = (CompilationUnitTree)parser.parse(file);
    check.init();
    List<Issue> actualIssues = check.analyze(file, tree);
    List<Issue> expectedIssues = getExpectedIssues(file, tree);
    compare(actualIssues, expectedIssues);
  }

  public static void check(PHPCheck check, File file, List<Issue> expectedIssues) {
    CompilationUnitTree tree = (CompilationUnitTree)parser.parse(file);
    check.init();
    List<Issue> actualIssues = check.analyze(file, tree);
    compare(actualIssues, expectedIssues);
  }

  private static void compare(List<Issue> actualIssues, List<Issue> expectedIssues) {
    Map<Integer, Tuple> map = new HashMap<>();

    for (Issue issue : actualIssues) {
      int line = issue.line();
      if (map.get(line) == null) {
        Tuple tuple = new Tuple();
        tuple.addActual(issue);
        map.put(line, tuple);
      } else {
        map.get(line).addActual(issue);
      }
    }

    for (Issue issue : expectedIssues) {
      int line = issue.line();
      if (map.get(line) == null) {
        Tuple tuple = new Tuple();
        tuple.addExpected(issue);
        map.put(line, tuple);
      } else {
        map.get(line).addExpected(issue);
      }
    }

    StringBuilder errorBuilder = new StringBuilder();
    for (Tuple tuple : map.values()) {
      errorBuilder.append(tuple.check());
    }

    String errorMessage = errorBuilder.toString();
    if (!StringUtils.isEmpty(errorMessage)) {
      throw new AssertionError("\n\n" + errorMessage);
    }
  }

  // NOK {{message here}}
  private static List<Issue> getExpectedIssues(File file, CompilationUnitTree tree) {
    IssueParser issueParser = new IssueParser();
    return issueParser.analyze(file, tree);
  }

  private static class IssueParser extends PHPVisitorCheck {

    @Override
    public void visitTrivia(SyntaxTrivia syntaxTrivia) {
      String text = syntaxTrivia.text();

      if (text.startsWith("// NOK {{")) {
        String message = text.substring(text.indexOf("{{") + 2, text.indexOf("}}"));
        context().newIssue("testKey", message).line(syntaxTrivia.line());
      } else if (text.startsWith("// NOK")) {
        context().newIssue("testKey", null).line(syntaxTrivia.line());
      }
    }

  }

  private static class Tuple {

    private static final String NO_ISSUE = "* [NO_ISSUE] Expected but no issue on line %s.\n\n";
    private static final String WRONG_MESSAGE = "* [WRONG_MESSAGE] Issue at line %s : \nExpected message : %s\nActual message : %s\n\n";
    private static final String UNEXPECTED_ISSUE = "* [UNEXPECTED_ISSUE] at line %s with a message: \"%s\"\n\n";
    private static final String WRONG_NUMBER = "* [WRONG_NUMBER] Line %s: Expecting %s issue, but actual issues number is %s\n\n";

    List<Issue> actual = new ArrayList<>();
    List<Issue> expected = new ArrayList<>();

    void addActual(Issue actual) {
      this.actual.add(actual);
    }

    void addExpected(Issue expected) {
      this.expected.add(expected);
    }

    String check() {
      if (!actual.isEmpty() && expected.isEmpty()) {
        return String.format(UNEXPECTED_ISSUE, actual.get(0).line(), actual.get(0).message());

      } else if (actual.isEmpty() && !expected.isEmpty()) {
        return String.format(NO_ISSUE, expected.get(0).line());

      } else if (actual.size() == 1 && expected.size() == 1) {
        String expectedMessage = expected.get(0).message();
        if (expectedMessage != null && !actual.get(0).message().equals(expectedMessage)) {
          return String.format(WRONG_MESSAGE, actual.get(0).line(), expectedMessage, actual.get(0).message());
        }

      } else if (actual.size() != expected.size()) {
        return String.format(WRONG_NUMBER, actual.get(0).line(), expected.size(), actual.size());

      } else {
        for (int i = 0; i < actual.size(); i++) {
          if (!actual.get(i).message().equals(expected.get(i).message())) {
            return String.format(WRONG_MESSAGE, actual.get(i).line(), expected.get(i).message(), actual.get(i).message());
          }
        }
      }

      return "";
    }

  }

}
