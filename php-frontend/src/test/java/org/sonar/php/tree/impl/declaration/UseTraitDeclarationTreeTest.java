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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

public class UseTraitDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  public void without_adaptations() throws Exception {
    UseTraitDeclarationTree tree = parse("use A,B,C;", Kind.USE_TRAIT_DECLARATION);
    assertThat(tree.is(Kind.USE_TRAIT_DECLARATION)).isTrue();
    assertThat(tree.traits()).hasSize(3);
    assertThat(tree.openCurlyBraceToken()).isNull();
    assertThat(tree.adaptations()).isEmpty();
    assertThat(tree.closeCurlyBraceToken()).isNull();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  public void with_adaptations() throws Exception {
    UseTraitDeclarationTree tree = parse("use A,B,C { m1 as m2; }", Kind.USE_TRAIT_DECLARATION);
    assertThat(tree.is(Kind.USE_TRAIT_DECLARATION)).isTrue();
    assertThat(tree.traits()).hasSize(3);
    assertThat(tree.openCurlyBraceToken().text()).isEqualTo("{");
    assertThat(tree.adaptations()).hasSize(1);
    assertThat(tree.closeCurlyBraceToken().text()).isEqualTo("}");
    assertThat(tree.eosToken()).isNull();
  }
}
