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
package org.sonar.php.tree.impl.statement;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class IfStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void standard_syntax() throws Exception {
    IfStatementTree tree = parse("if ($a) {}", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.ifToken().text()).isEqualTo("if");
    assertThat(expressionToString(tree.condition())).isEqualTo("($a)");
    assertThat(tree.statements()).hasSize(1);

    assertThat(tree.elseClause()).isNull();
    assertThat(tree.elseifClauses()).hasSize(0);
    assertThat(tree.endifToken()).isNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  public void standard_syntax_with_else() throws Exception {
    IfStatementTree tree = parse("if ($a) {} else {}", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.is(Kind.IF_STATEMENT)).isTrue();
    assertThat(tree.ifToken().text()).isEqualTo("if");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.statements()).hasSize(1);

    ElseClauseTree elseClause = tree.elseClause();
    assertThat(elseClause).isNotNull();
    assertThat(elseClause.is(Kind.ELSE_CLAUSE)).isTrue();
    assertThat(elseClause.statements()).hasSize(1);
    assertThat(tree.elseifClauses()).hasSize(0);
    assertThat(tree.colonToken()).isNull();

    assertThat(tree.endifToken()).isNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  public void alternative_syntax_with_else() throws Exception {
    IfStatementTree tree = parse("if ($a) : elseif ($a) : else : {} {} endif;", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_IF_STATEMENT)).isTrue();
    assertThat(tree.colonToken()).isNotNull();
    assertThat(tree.statements()).hasSize(0);

    assertThat(tree.elseClause()).isNotNull();

    assertThat(tree.elseifClauses()).hasSize(1);
    assertThat(tree.elseifClauses().get(0).is(Kind.ALTERNATIVE_ELSEIF_CLAUSE)).isTrue();

    assertThat(tree.endifToken()).isNotNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  public void elseif_syntax() throws Exception {
    IfStatementTree tree = parse("if ($a) {} elseif ($b) {} elseif ($c) {} else {}", PHPLexicalGrammar.IF_STATEMENT);

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.elseClause()).isNotNull();
    assertThat(tree.elseifClauses()).hasSize(2);
  }

}
