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
package org.sonar.plugins.php.api.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.sonar.sslr.api.typed.ActionParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.visitors.LegacyIssue;
import org.sonar.plugins.php.api.tests.TestIssue.Location;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.php.utils.ExpectedIssuesParser.parseExpectedIssues;


/**
 * Helper class for checks unit test.
 *
 * Code sample file: code_sample.php
 * When an issue is expected on a line, it should contains a comment as the following
 * <pre>
 *  {@literal<}?php
 *
 *  $a = 1;  // NOK {{expected issue message}}
 *  $a = 2;  // NOK
 * </pre>
 */
public class PHPCheckTest {

  private static final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  private PHPCheckTest() {
  }

  /**
   * Verifies that the given check raises issue as expected.
   *
   * @param check the tested check
   * @param file File containing the php code sample annotated with comment for expected issues.
   */
  public static void check(PHPCheck check, PhpFile file) {
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file.contents());
    check.init();
    List<PhpIssue> actualIssues = getActualIssues(check, file, tree);
    List<TestIssue> expectedIssues = parseExpectedIssues(file, tree);
    compare(actualIssues, expectedIssues);
  }

  /**
   * Verifies that the given check raises issue as expected.
   *
   * @param check the tested check
   * @param file File containing the php code sample
   * @param expectedIssues expected issues that should be raise. Overrides the comments in the code sample.
   */
  public static void check(PHPCheck check, PhpFile file, List<PhpIssue> expectedIssues) {
    CompilationUnitTree tree = (CompilationUnitTree)parser.parse(file.contents());
    check.init();
    List<PhpIssue> actualIssues = getActualIssues(check, file, tree);
    compare(actualIssues, toTestIssues(expectedIssues));
  }

  private static List<PhpIssue> getActualIssues(PHPCheck check, PhpFile file, CompilationUnitTree tree) {
    return check.analyze(file, tree);
  }

  private static List<TestIssue> toTestIssues(List<PhpIssue> phpIssues) {
    Builder<TestIssue> builder = ImmutableList.builder();

    for (PhpIssue phpIssue : phpIssues) {
      builder.add(TestIssue.create(message(phpIssue), line(phpIssue)));
    }

    return builder.build();
  }

  private static void compare(List<PhpIssue> actualIssues, List<TestIssue> expectedIssues) {
    Map<Integer, Tuple> map = new HashMap<>();

    for (PhpIssue issue : actualIssues) {
      int line = line(issue);
      if (map.get(line) == null) {
        Tuple tuple = new Tuple();
        tuple.addActual(issue);
        map.put(line, tuple);
      } else {
        map.get(line).addActual(issue);
      }
    }

    for (TestIssue issue : expectedIssues) {
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
    if (!errorMessage.isEmpty()) {
      throw new AssertionError("\n\n" + errorMessage);
    }
  }

  private static int line(PhpIssue issue) {
    if (issue instanceof LegacyIssue) {
      return ((LegacyIssue) issue).line();

    } else if (issue instanceof LineIssue) {
      return ((LineIssue) issue).line();

    } else if (issue instanceof FileIssue) {
      return 0;
    }

    return ((PreciseIssue) issue).primaryLocation().startLine();
  }

  private static String message(PhpIssue issue) {
    if (issue instanceof LegacyIssue) {
      return ((LegacyIssue) issue).message();

    } else if (issue instanceof LineIssue) {
      return ((LineIssue) issue).message();

    } else if (issue instanceof FileIssue) {
      return ((FileIssue) issue).message();
    }

    return ((PreciseIssue) issue).primaryLocation().message();
  }

  private static class Tuple {

    private static final String NO_ISSUE = "* [NO_ISSUE] Expected but no issue on line %s.\n\n";
    private static final String WRONG_MESSAGE = "* [WRONG_MESSAGE] Issue at line %s : \nExpected message : %s\nActual message : %s\n\n";
    private static final String WRONG_COST = "* [WRONG_COST] Issue at line %s : \nExpected cost : %s\nActual cost : %s\n\n";
    private static final String UNEXPECTED_ISSUE = "* [UNEXPECTED_ISSUE] at line %s with a message: \"%s\"\n\n";
    private static final String WRONG_NUMBER = "* [WRONG_NUMBER] Line %s: Expecting %s issue, but actual issues number is %s\n\n";
    private static final String WRONG_PRIMARY_LOCATION = "* [WRONG_PRIMARY_LOCATION] Line %s: actual %s column is %s\n\n";
    private static final String NO_PRECISE_LOCATION = "* [NO_PRECISE_LOCATION] Line %s: issue with precise location is expected\n\n";
    private static final String WRONG_SECONDARY_LOCATION = "* [WRONG_SECONDARY_LOCATION] Line %s: %s secondary location at line %s\n\n";

    List<PhpIssue> actual = new ArrayList<>();
    List<TestIssue> expected = new ArrayList<>();

    void addActual(PhpIssue actual) {
      this.actual.add(actual);
    }

    void addExpected(TestIssue expected) {
      this.expected.add(expected);
    }

    String check() {
      if (!actual.isEmpty() && expected.isEmpty()) {
        return String.format(UNEXPECTED_ISSUE, line(actual.get(0)), message(actual.get(0)));

      } else if (actual.isEmpty() && !expected.isEmpty()) {
        return String.format(NO_ISSUE, expected.get(0).line());

      } else if (actual.size() == 1 && expected.size() == 1) {
        TestIssue expectedIssue = expected.get(0);
        PhpIssue actualIssue = actual.get(0);
        return compareIssues(expectedIssue, actualIssue);

      } else if (actual.size() != expected.size()) {
        return String.format(WRONG_NUMBER, line(actual.get(0)), expected.size(), actual.size());

      } else {
        for (int i = 0; i < actual.size(); i++) {
          if (!message(actual.get(i)).equals(expected.get(i).message())) {
            return String.format(WRONG_MESSAGE, line(actual.get(i)), expected.get(i).message(), message(actual.get(i)));
          }
        }
      }

      return "";
    }

    private static String compareIssues(TestIssue expectedIssue, PhpIssue actualIssue) {
      String expectedMessage = expectedIssue.message();

      if (expectedMessage != null && !message(actualIssue).equals(expectedMessage)) {
        return String.format(WRONG_MESSAGE, line(actualIssue), expectedMessage, message(actualIssue));
      }

      Double expectedCost = expectedIssue.effortToFix();
      if (expectedCost != null && !expectedCost.equals(actualIssue.cost())) {
        return String.format(WRONG_COST, line(actualIssue), expectedCost, actualIssue.cost());
      }

      if (expectedIssue.startColumn() != null) {
        if (!(actualIssue instanceof PreciseIssue)) {
          return String.format(NO_PRECISE_LOCATION, expectedIssue.line());
        }

        PreciseIssue actualPreciseIssue = (PreciseIssue) actualIssue;
        int actualStart = actualPreciseIssue.primaryLocation().startLineOffset();
        if (expectedIssue.startColumn() != actualStart) {
          return String.format(WRONG_PRIMARY_LOCATION, expectedIssue.line(), "start", actualStart);
        }

        int actualEnd = actualPreciseIssue.primaryLocation().endLineOffset();
        if (expectedIssue.endColumn() != actualEnd) {
          return String.format(WRONG_PRIMARY_LOCATION, expectedIssue.line(), "end", actualEnd);
        }
      }

      if (!expectedIssue.secondaryLocations().isEmpty()) {
        return compareSecondary(actualIssue, expectedIssue);
      }

      return "";
    }

    private static String compareSecondary(PhpIssue actualIssue, TestIssue expectedIssue) {
      List<Location> expectedLocations = expectedIssue.secondaryLocations();
      List<IssueLocation> actualLocations = actualIssue instanceof PreciseIssue ? ((PreciseIssue) actualIssue).secondaryLocations() : new ArrayList<>();

      for (Location expected : expectedLocations) {
        IssueLocation actual = secondary(expected.line(), actualLocations);

        if (actual != null) {
          actualLocations.remove(actual);
        } else {
          return String.format(WRONG_SECONDARY_LOCATION, expectedIssue.line(), "missing", expected.line());
        }
      }

      if (!actualLocations.isEmpty()) {
        IssueLocation location = actualLocations.get(0);
        return String.format(WRONG_SECONDARY_LOCATION, location.startLine(), "unexpected", line(actualIssue));
      }

      return "";
    }

    private static IssueLocation secondary(int line, List<IssueLocation> allSecondaryLocations) {
      for (IssueLocation location : allSecondaryLocations) {
        if (location.startLine() == line) {
          return location;
        }
      }
      return null;
    }
  }

}
