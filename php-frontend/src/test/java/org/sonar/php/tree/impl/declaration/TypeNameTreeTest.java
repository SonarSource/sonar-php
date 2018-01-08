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
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeNameTreeTest extends PHPTreeModelTest {

  @Test
  public void built_in_type() throws Exception {
    BuiltInTypeTree tree = parse("int", PHPLexicalGrammar.TYPE_NAME);

    assertThat(tree.is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.token().text()).isEqualTo("int");
  }

  @Test
  public void built_in_type_capital_letter() throws Exception {
    BuiltInTypeTree tree = parse("Int", PHPLexicalGrammar.TYPE_NAME);

    assertThat(tree.is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.token().text()).isEqualTo("Int");
  }

  @Test
  public void built_in_type_keyword() throws Exception {
    BuiltInTypeTree tree = parse("callable", PHPLexicalGrammar.TYPE_NAME);

    assertThat(tree.is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.token().text()).isEqualTo("callable");
  }

  @Test
  public void namespace_name_type() throws Exception {
    NamespaceNameTree tree = parse("MyClass", PHPLexicalGrammar.TYPE_NAME);

    assertThat(tree.is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.fullName()).isEqualTo("MyClass");
  }
}
