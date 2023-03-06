/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import org.junit.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RegexCacheTest {

  @Test
  public void same_result_if_same_tree_is_provided() {
    CompilationUnitTree cut = ParsingTestUtils.parseSource("<?php" +
      "$s0 = '/abc/';" +
      "$s1 = '/abc/';"
    );

    ExpressionStatementTree statement1 = (ExpressionStatementTree) cut.script().statements().get(0);
    ExpressionStatementTree statement2 = (ExpressionStatementTree) cut.script().statements().get(1);

    LiteralTree s0 = (LiteralTree) ((AssignmentExpressionTree) statement1.expression()).value();
    LiteralTree s1 = (LiteralTree) ((AssignmentExpressionTree) statement2.expression()).value();

    RegexCache cache = new RegexCache();
    RegexParseResult resultForS0 = cache.getRegexForLiterals(new FlagSet(), s0);
    RegexParseResult resultForS1 = cache.getRegexForLiterals(new FlagSet(), s1);

    assertThat(s0.value()).isEqualTo(s1.value());
    assertThat(resultForS0)
      .isNotEqualTo(resultForS1)
      .isSameAs(cache.getRegexForLiterals(new FlagSet(), s0));

    assertThat(resultForS1).isSameAs(cache.getRegexForLiterals(new FlagSet(), s1));
  }

  @Test
  public void test_cache_via_context() {
    CompilationUnitTree cut = ParsingTestUtils.parseSource("<?php" +
      "$s0 = '/abc/';"
    );
    ExpressionStatementTree statement1 = (ExpressionStatementTree) cut.script().statements().get(0);
    LiteralTree s0 = (LiteralTree) ((AssignmentExpressionTree) statement1.expression()).value();

    PHPCheckContext phpCheckContext = new PHPCheckContext(mock(PhpFile.class), cut, null);

    RegexParseResult result1 = phpCheckContext.regexForLiteral(new FlagSet(), s0);
    RegexParseResult result2 = phpCheckContext.regexForLiteral(new FlagSet(), s0);
    assertThat(result1).isSameAs(result2);
  }

}
