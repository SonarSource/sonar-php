/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.MatchConditionClauseTree;
import org.sonar.plugins.php.api.tree.expression.MatchDefaultClauseTree;

import static org.assertj.core.api.Assertions.assertThat;

class MatchClauseTreeTest extends PHPTreeModelTest {

  @Test
  void singleConditionClause() {
    MatchConditionClauseTree tree = parse("$a=>$b", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_CONDITION_CLAUSE)).isTrue();
    assertThat(tree.conditions()).hasSize(1);
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
  }

  @Test
  void multiConditionClause() {
    MatchConditionClauseTree tree = parse("$a,$b,$c,=>4", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_CONDITION_CLAUSE)).isTrue();
    assertThat(tree.conditions()).hasSize(3);
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
  }

  @Test
  void defaultClause() {
    MatchDefaultClauseTree tree = parse("default => false", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_DEFAULT_CLAUSE)).isTrue();
    assertThat(tree.defaultToken().text()).isEqualTo("default");
    assertThat(tree.trailingComma()).isNull();
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.expression())).isEqualTo("false");
  }

  @Test
  void defaultClauseWithTrailingComma() {
    MatchDefaultClauseTree tree = parse("default, => 1", PHPLexicalGrammar.MATCH_CLAUSE);

    assertThat(tree.is(Kind.MATCH_DEFAULT_CLAUSE)).isTrue();
    assertThat(tree.defaultToken().text()).isEqualTo("default");
    assertThat(tree.trailingComma().text()).isEqualTo(",");
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.expression())).isEqualTo("1");
  }
}
