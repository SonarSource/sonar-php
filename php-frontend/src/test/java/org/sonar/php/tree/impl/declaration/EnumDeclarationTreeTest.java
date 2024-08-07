/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.EnumDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

class EnumDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void simpleEnumWithNoCases() {
    EnumDeclarationTreeImpl tree = parse("enum A {}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.is(Tree.Kind.ENUM_DECLARATION)).isTrue();
    assertThat(tree.childrenIterator()).toIterable().hasSize(7);
    assertThat(tree.classToken()).hasToString("enum");
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.typeColonToken()).isNull();
    assertThat(tree.backingType()).isNull();
    assertThat(tree.openCurlyBraceToken()).hasToString("{");
    assertThat(tree.cases()).isEmpty();
    assertThat(tree.members()).isEmpty();
    assertThat(tree.closeCurlyBraceToken()).hasToString("}");
  }

  @Test
  void simpleEnumWithCases() {
    EnumDeclarationTree tree = parse("enum A {case A;\ncase B;}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.is(Tree.Kind.ENUM_DECLARATION)).isTrue();
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.cases()).hasSize(2);
    assertThat(tree.members()).hasSize(2);
    assertThat(tree.cases().get(0).name()).hasToString("A");
    assertThat(tree.cases().get(1).name()).hasToString("B");
  }

  @Test
  void enumCanContainOtherClassLikeMembers() {
    EnumDeclarationTree tree = parse("enum A {case A;\nconst FOO = 1;\npublic function foo(){} }", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.cases()).hasSize(1);
    assertThat(tree.cases().get(0).name()).hasToString("A");
    assertThat(tree.members()).hasSize(3);
    assertThat(tree.members().get(0)).isSameAs(tree.cases().get(0));
    assertThat(tree.members().get(1).is(Tree.Kind.CLASS_CONSTANT_PROPERTY_DECLARATION)).isTrue();
    assertThat(tree.members().get(2).is(Tree.Kind.METHOD_DECLARATION)).isTrue();
  }

  @Test
  void enumCanImplementInterfaces() {
    EnumDeclarationTree tree = parse("enum A implements B,C {}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.implementsToken()).hasToString("implements");
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.superInterfaces().get(0)).hasToString("B");
    assertThat(tree.superInterfaces().get(1)).hasToString("C");
  }

  @Test
  void enumCanHaveAttributes() {
    EnumDeclarationTree tree = parse("#[A1(1)] enum A {}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes().get(0).name()).hasToString("A1");
  }

  @Test
  void enumWithBackingType() {
    EnumDeclarationTree tree = parse("enum A: string {}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.typeColonToken()).hasToString(":");
    assertThat(tree.backingType()).hasToString("string");
  }
}
