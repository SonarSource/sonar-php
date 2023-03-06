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

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.MatchConditionClauseTree;
import org.sonar.plugins.php.api.tree.expression.MatchDefaultClauseTree;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchClauseTreeTest extends PHPTreeModelTest {

  @Test
  public void single_condition_clause() throws Exception {
    MatchConditionClauseTree tree = parse("$a=>$b", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_CONDITION_CLAUSE)).isTrue();
    assertThat(tree.conditions()).hasSize(1);
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
  }

  @Test
  public void multi_condition_clause() throws Exception {
    MatchConditionClauseTree tree = parse("$a,$b,$c,=>4", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_CONDITION_CLAUSE)).isTrue();
    assertThat(tree.conditions()).hasSize(3);
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
  }

  @Test
  public void default_clause() throws Exception {
    MatchDefaultClauseTree tree = parse("default => false", PHPLexicalGrammar.MATCH_CLAUSE);
    
    assertThat(tree.is(Kind.MATCH_DEFAULT_CLAUSE)).isTrue();
    assertThat(tree.defaultToken().text()).isEqualTo("default");
    assertThat(tree.trailingComma()).isNull();
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.expression())).isEqualTo("false");
  }

  @Test
  public void default_clause_with_trailing_comma() throws Exception {
    MatchDefaultClauseTree tree = parse("default, => 1", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_DEFAULT_CLAUSE)).isTrue();
    assertThat(tree.defaultToken().text()).isEqualTo("default");
    assertThat(tree.trailingComma().text()).isEqualTo(",");
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.expression())).isEqualTo("1");
  }
}
