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
package org.sonar.php.regex;

import javax.annotation.Nullable;
import org.opentest4j.AssertionFailedError;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexParserTestUtils {

  private RegexParserTestUtils() {
  }

  public static RegexParseResult parseRegex(String regex) {
    RegexSource source = makeSource(regex);
    return new RegexParser(source, new FlagSet()).parse();
  }

  public static RegexTree assertSuccessfulParse(String regex) {
    RegexParseResult result = parseRegex(regex);
    if (!result.getSyntaxErrors().isEmpty()) {
      throw new AssertionFailedError("Parsing should complete with no errors.");
    }
    return result.getResult();
  }

  // place the String which will contain the regex on 3rd line, starting from index 0
  private static final String PHP_CODE = "<?php\nfoo(\n%s\n);";

  public static RegexSource makeSource(String content) {
    CompilationUnitTree tree = ParsingTestUtils.parseSource(String.format(PHP_CODE, content));
    ExpressionStatementTree statement = (ExpressionStatementTree) tree.script().statements().get(0);
    FunctionCallTree call = (FunctionCallTree) statement.expression();
    LiteralTree expr = (LiteralTree) call.callArguments().get(0).value();
    return new PhpAnalyzerRegexSource(expr);
  }

  public static void assertKind(RegexTree.Kind expected, RegexTree actual) {
    assertThat(actual.kind()).withFailMessage(String.format("Regex should have kind %s", expected)).isEqualTo(expected);

    assertThat(actual.is(expected)).withFailMessage("`is` should return true when the kinds match.").isTrue();
    assertThat(actual.is(RegexTree.Kind.CHARACTER, RegexTree.Kind.DISJUNCTION, expected))
      .withFailMessage("`is` should return true when one of the kinds match.").isTrue();
  }

  public static void assertKind(CharacterClassElementTree.Kind expected, CharacterClassElementTree actual) {
    assertThat(actual.characterClassElementKind()).withFailMessage(String.format("Regex should have kind %s", expected)).isEqualTo(expected);

    assertThat(actual.is(expected)).withFailMessage("`is` should return true when the kinds match.").isTrue();
    assertThat(actual.is(CharacterClassElementTree.Kind.PLAIN_CHARACTER, expected))
      .withFailMessage("`is` should return true when one of the kinds match.").isTrue();
  }

  public static <T> T assertType(Class<T> klass, @Nullable Object o) {
    if (o == null) {
      throw new AssertionFailedError("Object should not be null.");
    }
    String actual = o.getClass().getSimpleName();
    String expected = klass.getSimpleName();
    if (!klass.isInstance(o)) {
      throw new AssertionFailedError(String.format("Object should have the correct type. [expected: %s, actual: %s]", expected, actual));
    }
    return klass.cast(o);
  }
}
