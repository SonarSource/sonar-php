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
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

import static org.assertj.core.api.Assertions.assertThat;

public class NamespaceNameTreeTest extends PHPTreeModelTest {

  @Test
  public void absolute() throws Exception {
    NamespaceNameTree tree = parse("\\ns1\\ns2\\name", PHPLexicalGrammar.NAMESPACE_NAME);

    assertThat(tree.is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.absoluteSeparator()).isNotNull();
    assertThat(tree.name().text()).isEqualTo("name");
    assertThat(tree.namespaces()).hasSize(2);
    assertThat(tree.fullName()).isEqualTo("\\ns1\\ns2\\name");
  }

  @Test
  public void relative() throws Exception {
    NamespaceNameTree tree = parse("ns1\\ns2\\name", PHPLexicalGrammar.NAMESPACE_NAME);

    assertThat(tree.is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.absoluteSeparator()).isNull();
    assertThat(tree.name().text()).isEqualTo("name");
    assertThat(tree.namespaces()).hasSize(2);
    assertThat(tree.fullName()).isEqualTo("ns1\\ns2\\name");
  }

  @Test
  public void with_namespace_keyword() throws Exception {
    NamespaceNameTree tree = parse("namespace\\subNS\\name", PHPLexicalGrammar.NAMESPACE_NAME);
    assertThat(tree.namespaces().get(0).text()).isEqualTo("namespace");
  }

  @Test
  public void test_simple() throws Exception {
    parse("\\a", PHPLexicalGrammar.NAMESPACE_NAME);
  }
}
