/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.EnumDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  public void simple_enum_with_no_cases() {
    EnumDeclarationTreeImpl tree = parse("enum A {}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.is(Tree.Kind.ENUM_DECLARATION)).isTrue();
    assertThat(tree.childrenIterator()).hasSize(4);
    assertThat(tree.classToken()).hasToString("enum");
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.openCurlyBraceToken()).hasToString("{");
    assertThat(tree.cases()).isEmpty();
    assertThat(tree.members()).isEmpty();
    assertThat(tree.closeCurlyBraceToken()).hasToString("}");
  }

  @Test
  public void simple_enum_with_cases() {
    EnumDeclarationTree tree = parse("enum A {case A;\ncase B;}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.is(Tree.Kind.ENUM_DECLARATION)).isTrue();
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.cases()).hasSize(2);
    assertThat(tree.members()).hasSize(2);
    assertThat(tree.cases().get(0).name()).hasToString("A");
    assertThat(tree.cases().get(1).name()).hasToString("B");
  }

  @Test
  public void enum_can_contain_other_class_like_members() {
    EnumDeclarationTree tree = parse("enum A {case A;\nconst FOO = 1;\npublic function foo(){} }", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.cases()).hasSize(1);
    assertThat(tree.cases().get(0).name()).hasToString("A");
    assertThat(tree.members()).hasSize(3);
    assertThat(tree.members().get(0)).isSameAs(tree.cases().get(0));
    assertThat(tree.members().get(1).is(Tree.Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.members().get(2).is(Tree.Kind.METHOD_DECLARATION)).isTrue();
  }
}
