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
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class SwitchStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void standard_syntax() throws Exception {
    SwitchStatementTree tree = parse("switch ($a) { case $a : break; default : break; }", PHPLexicalGrammar.SWITCH_STATEMENT);

    assertThat(tree.is(Kind.SWITCH_STATEMENT)).isTrue();
    assertThat(tree.switchToken().text()).isEqualTo("switch");
    assertThat(expressionToString(tree.expression())).isEqualTo("($a)");
    assertThat(tree.cases()).hasSize(2);

    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endswitchToken()).isNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  public void alternative_syntax() throws Exception {
    SwitchStatementTree tree = parse("switch ($a) : default : break; endswitch;", PHPLexicalGrammar.SWITCH_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_SWITCH_STATEMENT)).isTrue();
    assertThat(tree.switchToken().text()).isEqualTo("switch");
    assertThat(expressionToString(tree.expression())).isEqualTo("($a)");
    assertThat(tree.cases()).hasSize(1);

    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(tree.endswitchToken().text()).isEqualTo("endswitch");
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }



}
