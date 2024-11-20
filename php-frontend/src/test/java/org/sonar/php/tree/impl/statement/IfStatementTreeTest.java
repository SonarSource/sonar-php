/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class IfStatementTreeTest extends PHPTreeModelTest {

  @Test
  void standardSyntax() {
    IfStatementTree tree = parse("if ($a) {}", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.ifToken().text()).isEqualTo("if");
    assertThat(expressionToString(tree.condition())).isEqualTo("($a)");
    assertThat(tree.statements()).hasSize(1);

    assertThat(tree.elseClause()).isNull();
    assertThat(tree.elseifClauses()).isEmpty();
    assertThat(tree.endifToken()).isNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  void standardSyntaxWithElse() {
    IfStatementTree tree = parse("if ($a) {} else {}", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.is(Kind.IF_STATEMENT)).isTrue();
    assertThat(tree.ifToken().text()).isEqualTo("if");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.statements()).hasSize(1);

    ElseClauseTree elseClause = tree.elseClause();
    assertThat(elseClause).isNotNull();
    assertThat(elseClause.is(Kind.ELSE_CLAUSE)).isTrue();
    assertThat(elseClause.statements()).hasSize(1);
    assertThat(tree.elseifClauses()).isEmpty();
    assertThat(tree.colonToken()).isNull();

    assertThat(tree.endifToken()).isNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  void alternativeSyntaxWithElse() {
    IfStatementTree tree = parse("if ($a) : elseif ($a) : else : {} {} endif;", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_IF_STATEMENT)).isTrue();
    assertThat(tree.colonToken()).isNotNull();
    assertThat(tree.statements()).isEmpty();

    assertThat(tree.elseClause()).isNotNull();

    assertThat(tree.elseifClauses()).hasSize(1);
    assertThat(tree.elseifClauses().get(0).is(Kind.ALTERNATIVE_ELSEIF_CLAUSE)).isTrue();

    assertThat(tree.endifToken()).isNotNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  void elseifSyntax() {
    IfStatementTree tree = parse("if ($a) {} elseif ($b) {} elseif ($c) {} else {}", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.elseClause()).isNotNull();
    assertThat(tree.elseifClauses()).hasSize(2);
  }

}
