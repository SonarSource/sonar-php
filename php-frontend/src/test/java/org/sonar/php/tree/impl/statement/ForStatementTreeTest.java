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
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ForStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void standard_syntax() throws Exception {
    ForStatementTree tree = parse("for ($a; ; $c, $b) {}", PHPLexicalGrammar.FOR_STATEMENT);

    assertThat(tree.is(Kind.FOR_STATEMENT)).isTrue();
    assertThat(tree.forToken().text()).isEqualTo("for");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.init()).hasSize(1);
    assertThat(tree.condition()).hasSize(0);
    assertThat(tree.update()).hasSize(2);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endforToken()).isNull();
    assertThat(tree.eosToken()).isNull();
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(Kind.BLOCK)).isTrue();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  public void alternative_syntax() throws Exception {
    ForStatementTree tree = parse("for (; ;) : endfor ;", PHPLexicalGrammar.FOR_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_FOR_STATEMENT)).isTrue();
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.init()).hasSize(0);
    assertThat(tree.condition()).hasSize(0);
    assertThat(tree.update()).hasSize(0);
    assertThat(tree.colonToken()).isNotNull();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.endforToken()).isNotNull();
    assertThat(tree.eosToken()).isNotNull();
    assertThat(tree.statements()).hasSize(0);
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }
}
