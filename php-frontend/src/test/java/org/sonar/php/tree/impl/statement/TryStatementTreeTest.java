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
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class TryStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void test_2_catch_blocks() throws Exception {
    TryStatementTree tree = parse("try {} catch(Exception1 $e1) {} catch(Exception2 $e2) {}", PHPLexicalGrammar.TRY_STATEMENT);

    assertThat(tree.is(Kind.TRY_STATEMENT)).isTrue();
    assertThat(tree.tryToken().text()).isEqualTo("try");
    assertThat(expressionToString(tree.block())).isEqualTo("{}");
    assertThat(tree.catchBlocks()).hasSize(2);
    assertThat(tree.finallyToken()).isNull();
    assertThat(tree.finallyBlock()).isNull();
  }

  @Test
  public void test_finally_block() throws Exception {
    TryStatementTree tree = parse("try {} finally {}", PHPLexicalGrammar.TRY_STATEMENT);

    assertThat(tree.is(Kind.TRY_STATEMENT)).isTrue();
    assertThat(tree.tryToken().text()).isEqualTo("try");
    assertThat(expressionToString(tree.block())).isEqualTo("{}");
    assertThat(tree.catchBlocks()).hasSize(0);
    assertThat(tree.finallyToken().text()).isEqualTo("finally");
    assertThat(expressionToString(tree.finallyBlock())).isEqualTo("{}");
  }

}
