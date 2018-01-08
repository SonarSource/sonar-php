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
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class StaticStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    StaticStatementTree tree = parse("static $a, $b = $c;", PHPLexicalGrammar.STATIC_STATEMENT);

    assertThat(tree.is(Kind.STATIC_STATEMENT)).isTrue();
    assertThat(tree.staticToken().text()).isEqualTo("static");
    assertThat(tree.variables()).hasSize(2);
    assertThat(tree.variables().get(0).equalToken()).isNull();
    assertThat(tree.variables().get(1).equalToken()).isNotNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
