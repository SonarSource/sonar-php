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
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.MatchExpressionTree;

import static org.assertj.core.api.Assertions.assertThat;

class MatchExpressionTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    MatchExpressionTree tree = parse("match ($a) { 0,1 => 'Foo', default=>'bar'}", PHPLexicalGrammar.MATCH_EXPRESSION);

    assertThat(tree.is(Kind.MATCH_EXPRESSION)).isTrue();
    assertThat(tree.matchToken().text()).isEqualTo("match");
    assertThat(expressionToString(tree.expression())).isEqualTo("$a");
    assertThat(tree.cases()).hasSize(2);
    assertThat(tree.openCurlyBraceToken().text()).isEqualTo("{");
    assertThat(tree.closeCurlyBraceToken().text()).isEqualTo("}");
    assertThat(tree.openParenthesis().text()).isEqualTo("(");
    assertThat(tree.closeParenthesis().text()).isEqualTo(")");
  }

  @Test
  void testWithTrailingComma() {
    MatchExpressionTree tree = parse("match ($var->field) { 0 => 'Foo',}", PHPLexicalGrammar.MATCH_EXPRESSION);

    assertThat(tree.is(Kind.MATCH_EXPRESSION)).isTrue();
    assertThat(tree.matchToken().text()).isEqualTo("match");
    assertThat(expressionToString(tree.expression())).isEqualTo("$var->field");
    assertThat(tree.cases()).hasSize(1);
  }
}
