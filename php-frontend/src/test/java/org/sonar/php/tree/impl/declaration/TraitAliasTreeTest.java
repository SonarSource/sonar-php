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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.TraitAliasTree;

public class TraitAliasTreeTest extends PHPTreeModelTest {

  @Test
  public void with_alias() throws Exception {
    TraitAliasTree tree = alias("method1 as method2;");
    assertThat(tree.methodReference().method().text()).isEqualTo("method1");
    assertThat(tree.modifierToken()).isNull();
    assertThat(tree.alias().text()).isEqualTo("method2");
  }

  @Test
  public void with_modifier() throws Exception {
    TraitAliasTree tree = alias("method1 as public;");
    assertThat(tree.methodReference().method().text()).isEqualTo("method1");
    assertThat(tree.modifierToken().text()).isEqualTo("public");
    assertThat(tree.alias()).isNull();
  }

  @Test
  public void with_alias_and_modifier() throws Exception {
    TraitAliasTree tree = alias("method1 as public method2;");
    assertThat(tree.methodReference().method().text()).isEqualTo("method1");
    assertThat(tree.modifierToken().text()).isEqualTo("public");
    assertThat(tree.alias().text()).isEqualTo("method2");
  }

  private TraitAliasTree alias(String toParse) throws Exception {
    TraitAliasTree tree = parse(toParse, PHPLexicalGrammar.TRAIT_ALIAS);
    assertThat(tree.is(Kind.TRAIT_ALIAS)).isTrue();
    assertThat(tree.asToken().text()).isEqualTo("as");
    assertThat(tree.eosToken().text()).isEqualTo(";");
    return tree;
  }

}
