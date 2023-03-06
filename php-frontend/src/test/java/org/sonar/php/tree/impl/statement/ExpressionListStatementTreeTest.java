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
import org.sonar.plugins.php.api.tree.statement.ExpressionListStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpressionListStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    ExpressionListStatementTree tree = parse("$a, foo();", PHPLexicalGrammar.EXPRESSION_LIST_STATEMENT);

    assertThat(tree.is(Kind.EXPRESSION_LIST_STATEMENT)).isTrue();
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.expressions().get(0).is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.expressions().get(1).is(Kind.FUNCTION_CALL)).isTrue();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
