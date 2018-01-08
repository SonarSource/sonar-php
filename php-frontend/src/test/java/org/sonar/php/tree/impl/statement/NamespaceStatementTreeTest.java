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
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class NamespaceStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void standard_syntax() throws Exception {
    NamespaceStatementTree tree = parse("namespace NS;", PHPLexicalGrammar.NAMESPACE_STATEMENT);

    assertThat(tree.is(Kind.NAMESPACE_STATEMENT)).isTrue();
    assertThat(tree.namespaceToken().text()).isEqualTo("namespace");
    assertThat(expressionToString(tree.namespaceName())).isEqualTo("NS");
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.statements()).isEmpty();
    assertThat(tree.openCurlyBrace()).isNull();
    assertThat(tree.closeCurlyBrace()).isNull();
  }
  @Test
  public void block_syntax() throws Exception {
    NamespaceStatementTree tree = parse("namespace { $a; }", PHPLexicalGrammar.NAMESPACE_STATEMENT);

    assertThat(tree.is(Kind.NAMESPACE_STATEMENT)).isTrue();
    assertThat(tree.namespaceName()).isNull();

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.openCurlyBrace()).isNotNull();
    assertThat(tree.closeCurlyBrace()).isNotNull();
  }

}
