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
package org.sonar.php.regex;

import java.util.List;
import org.junit.Test;
import org.sonarsource.analyzer.commons.regex.RegexDialect;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.CharacterTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.sonar.php.regex.RegexParserTestUtils.assertKind;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.php.regex.RegexParserTestUtils.makeSource;

public class PhpRegexSourceTest {

  @Test
  // TODO: Extend test with exact syntax error location check
  public void invalid_regex() {
    RegexSource source = makeSource("'/+/'");
    RegexParseResult result = new RegexParser(source, new FlagSet()).parse();

    assertThat(result.getSyntaxErrors()).isNotEmpty();
  }

  @Test
  public void test_missing_delimiters() {
    assertThatThrownBy(() -> makeSource("'a\nb'"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Regular expression does not contain delimiters");
  }

  @Test
  public void test_non_string_literal() {
    assertThatThrownBy(() -> makeSource("1"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only string literals allowed");
  }

  @Test
  // TODO: Extend test with character location checks
  public void test_string_literal() {
    RegexTree regex = assertSuccessfulParse("'/a\nb/'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items).hasSize(3);

    assertCharacter('a', items.get(0));
    assertCharacter('\n', items.get(1));
    assertCharacter('b', items.get(2));
  }

  @Test
  public void test_dialect() {
    RegexSource source = makeSource("'/a/'");
    assertEquals(RegexDialect.PHP, source.dialect());
  }

  private static void assertCharacter(char expected, RegexTree tree) {
    assertKind(RegexTree.Kind.CHARACTER, tree);
    assertEquals(expected, ((CharacterTree) tree).codePointOrUnit());
  }

}
