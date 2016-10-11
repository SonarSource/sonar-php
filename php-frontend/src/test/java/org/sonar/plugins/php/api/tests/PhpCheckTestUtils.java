/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.apache.commons.lang.StringUtils;
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.visitors.PHPIssue;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.CheckIssue;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

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
public class PhpCheckTestUtils {

  private static final Charset charset = Charsets.UTF_8;

  private static final ActionParser<Tree> parser = PHPParserBuilder.createParser(charset);

  private PhpCheckTestUtils() {
  }

  /**
   * Verifies that the given check raises issue as expected.
   *
   * @param check the tested check
   * @param file File containing the php code sample annotated with comment for expected issues.
   */
  public static void check(PHPCheck check, File file) {
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file);
    check.init();
    List<CheckIssue> actualIssues = getActualIssues(check, file, tree);
    List<CheckIssue> expectedIssues = getExpectedIssues(check, file, tree);
    compare(actualIssues, expectedIssues);
  }

  private static List<CheckIssue> getActualIssues(PHPCheck check, File file, CompilationUnitTree tree) {
    if (check instanceof CharsetAwareVisitor) {
      ((CharsetAwareVisitor) check).setCharset(charset);
    }
    return check.analyze(file, tree);
  }

  /**
   * Verifies that the given check raises issue as expected.
   *
   * @param check the tested check
   * @param file File containing the php code sample
   * @param expectedIssues expected issues that should be raise. Overrides the comments in the code sample.
   */
  public static void check(PHPCheck check, File file, List<CheckIssue> expectedIssues) {
    CompilationUnitTree tree = (CompilationUnitTree)parser.parse(file);
    check.init();
    List<CheckIssue> actualIssues = getActualIssues(check, file, tree);
    compare(actualIssues, expectedIssues);
  }

  private static void compare(List<CheckIssue> actualIssues, List<CheckIssue> expectedIssues) {
    Map<Integer, Tuple> map = new HashMap<>();

    for (CheckIssue issue : actualIssues) {
      int line = line(issue);
      if (map.get(line) == null) {
        Tuple tuple = new Tuple();
        tuple.addActual(issue);
        map.put(line, tuple);
      } else {
        map.get(line).addActual(issue);
      }
    }

    for (CheckIssue issue : expectedIssues) {
      int line = line(issue);
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
  private static List<CheckIssue> getExpectedIssues(PHPCheck check, File file, CompilationUnitTree tree) {
    IssueParser issueParser = new IssueParser(check);
    return issueParser.analyze(file, tree);
  }

  private static class IssueParser extends PHPVisitorCheck {

    private final PHPCheck check;

    public IssueParser(PHPCheck check) {
      this.check = check;
    }

    @Override
    public void visitTrivia(SyntaxTrivia syntaxTrivia) {
      String text = syntaxTrivia.text();

      if (text.startsWith("// NOK")) {
        String message = null;
        Integer effortToFix = null;

        int expectedMessageIndex = text.indexOf("{{");
        if (expectedMessageIndex > -1) {
          message = text.substring(expectedMessageIndex + 2, text.indexOf("}}"));
        }

        String effortToFixStartMarker = "[[effortToFix=";
        int effortToFixIndex = text.indexOf(effortToFixStartMarker);
        if (effortToFixIndex > -1) {
          String remaining = text.substring(effortToFixIndex + effortToFixStartMarker.length());
          int effortToFixEndIndex = remaining.indexOf("]]");
          if (effortToFixEndIndex == -1) {
            throw new IllegalStateException("No end marker for effortToFix in: " + text);
          }
          String effortToFixString = remaining.substring(0, effortToFixEndIndex);
          try {
            effortToFix = Integer.parseInt(effortToFixString, 10);
          } catch (NumberFormatException e) {
            throw new IllegalStateException("Could not parse effortToFix: " + effortToFixString, e);
          }
        }

        CheckIssue issue = context().newIssue(check, message).line(syntaxTrivia.line());
        if (effortToFix != null) {
          issue.cost(effortToFix);
        }
      }
    }

  }

  private static int line(CheckIssue issue) {
    if (issue instanceof PHPIssue) {
      return ((PHPIssue) issue).line();

    } else if (issue instanceof LineIssue) {
      return ((LineIssue) issue).line();

    } else if (issue instanceof FileIssue) {
      return 0;
    }

    return ((PreciseIssue) issue).primaryLocation().startLine();
  }

  private static String message(CheckIssue issue) {
    if (issue instanceof PHPIssue) {
      return ((PHPIssue) issue).message();

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

    List<CheckIssue> actual = new ArrayList<>();
    List<CheckIssue> expected = new ArrayList<>();

    void addActual(CheckIssue actual) {
      this.actual.add(actual);
    }

    void addExpected(CheckIssue expected) {
      this.expected.add(expected);
    }

    String check() {
      if (!actual.isEmpty() && expected.isEmpty()) {
        return String.format(UNEXPECTED_ISSUE, line(actual.get(0)), message(actual.get(0)));

      } else if (actual.isEmpty() && !expected.isEmpty()) {
        return String.format(NO_ISSUE, line(expected.get(0)));

      } else if (actual.size() == 1 && expected.size() == 1) {
        String expectedMessage = message(expected.get(0));
        if (expectedMessage != null && !message(actual.get(0)).equals(expectedMessage)) {
          return String.format(WRONG_MESSAGE, line(actual.get(0)), expectedMessage, message(actual.get(0)));
        }

        Double expectedCost = expected.get(0).cost();
        if (expectedCost != null && !expectedCost.equals(actual.get(0).cost())) {
          return String.format(WRONG_COST, line(actual.get(0)), expectedCost, actual.get(0).cost());
        }

      } else if (actual.size() != expected.size()) {
        return String.format(WRONG_NUMBER, line(actual.get(0)), expected.size(), actual.size());

      } else {
        for (int i = 0; i < actual.size(); i++) {
          if (!message(actual.get(i)).equals(message(expected.get(i)))) {
            return String.format(WRONG_MESSAGE, line(actual.get(i)), message(expected.get(i)), message(actual.get(i)));
          }
        }
      }

      return "";
    }

  }

}
