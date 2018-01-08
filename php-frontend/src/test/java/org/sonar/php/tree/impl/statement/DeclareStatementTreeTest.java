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
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class DeclareStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void short_syntax() throws Exception {
    DeclareStatementTree tree = parse("declare (a);", PHPLexicalGrammar.DECLARE_STATEMENT);

    assertThat(tree.is(Kind.DECLARE_STATEMENT)).isTrue();
    assertThat(tree.declareToken().text()).isEqualTo("declare");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.directives()).hasSize(1);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endDeclareToken()).isNull();
    assertThat(tree.eosToken()).isNotNull();
    assertThat(tree.statements()).hasSize(0);
  }

  @Test
  public void one_statement_syntax() throws Exception {
    DeclareStatementTree tree = parse("declare (a = $a, b = $b) {}", PHPLexicalGrammar.DECLARE_STATEMENT);

    assertThat(tree.is(Kind.DECLARE_STATEMENT)).isTrue();
    assertThat(tree.declareToken().text()).isEqualTo("declare");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.directives()).hasSize(2);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endDeclareToken()).isNull();
    assertThat(tree.eosToken()).isNull();
    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  public void alternative_syntax() throws Exception {
    DeclareStatementTree tree = parse("declare (a) : {} {} enddeclare ;", PHPLexicalGrammar.DECLARE_STATEMENT);

    assertThat(tree.is(Kind.DECLARE_STATEMENT)).isTrue();
    assertThat(tree.directives()).hasSize(1);
    assertThat(tree.colonToken()).isNotNull();
    assertThat(tree.endDeclareToken()).isNotNull();
    assertThat(tree.eosToken()).isNotNull();
    assertThat(tree.statements()).hasSize(2);
  }

}
