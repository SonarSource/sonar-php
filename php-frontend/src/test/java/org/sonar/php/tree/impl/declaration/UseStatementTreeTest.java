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
package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

public class UseStatementTreeTest extends PHPTreeModelTest {

  @Test
  public void single_declaration() throws Exception {
    UseStatementTree tree = parse("use \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_STATEMENT);
    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken()).isNull();
    assertThat(tree.prefix()).isNull();
    assertThat(tree.nsSeparatorToken()).isNull();
    assertThat(tree.openCurlyBraceToken()).isNull();
    assertThat(tree.closeCurlyBraceToken()).isNull();
    assertThat(tree.clauses()).hasSize(1);
  }

  @Test
  public void multiple_declarations() throws Exception {
    UseStatementTree tree = parse("use \\ns1\\ns2\\name, \\ns1\\ns2\\name2;", PHPLexicalGrammar.USE_STATEMENT);
    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken()).isNull();
    assertThat(tree.clauses()).hasSize(2);
  }
  
  @Test
  public void const_token() throws Exception {
    UseStatementTree tree = parse("use const \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_STATEMENT);

    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken().text()).isEqualTo("const");
  }

  @Test
  public void function_token() throws Exception {
    UseStatementTree tree = parse("use function \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_STATEMENT);

    assertThat(tree.is(Kind.USE_STATEMENT)).isTrue();
    assertThat(tree.useTypeToken().text()).isEqualTo("function");
  }

  @Test
  public void group_use_statement() throws Exception {
    UseStatementTree tree = parse("use ns1\\ns2\\{name1 as a1, function name2};", Kind.GROUP_USE_STATEMENT);
    assertThat(tree.is(Kind.GROUP_USE_STATEMENT)).isTrue();
    assertThat(tree.prefix()).isNotNull();
    assertThat(expressionToString(tree.prefix())).isEqualTo("ns1\\ns2");
    assertThat(tree.nsSeparatorToken()).isNotNull();
    assertThat(tree.openCurlyBraceToken()).isNotNull();
    assertThat(tree.closeCurlyBraceToken()).isNotNull();
    assertThat(tree.clauses()).hasSize(2);
  }


  @Test
  public void with_trailing_comma() throws Exception {
    UseStatementTree tree = parse("use ns1\\{A, B, C,};", Kind.GROUP_USE_STATEMENT);
    assertThat(tree.is(Kind.GROUP_USE_STATEMENT)).isTrue();
    assertThat(tree.prefix()).isNotNull();
    assertThat(tree.clauses()).hasSize(3);
  }
}
