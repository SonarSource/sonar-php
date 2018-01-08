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
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ContinueStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void empty() throws Exception {
    ContinueStatementTree tree = parse("continue ;", PHPLexicalGrammar.CONTINUE_STATEMENT);

    assertThat(tree.is(Kind.CONTINUE_STATEMENT)).isTrue();
    assertThat(tree.continueToken().text()).isEqualTo("continue");
    assertThat(tree.argument()).isNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  public void not_empty() throws Exception {
    ContinueStatementTree tree = parse("continue $a;", PHPLexicalGrammar.CONTINUE_STATEMENT);

    assertThat(tree.continueToken().text()).isEqualTo("continue");
    assertThat(tree.argument()).isNotNull();
    assertThat(tree.argument().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
