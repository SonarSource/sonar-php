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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;

import static org.assertj.core.api.Assertions.assertThat;

class ClassDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void fullClassDeclaration() {
    ClassDeclarationTree tree = parse("final class A extends B implements C, D { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);

    assertThat(tree.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree.modifiersToken()).hasSize(1);
    assertThat(tree.modifiersToken().get(0).text()).isEqualTo("final");
    assertThat(tree.classToken().text()).isEqualTo("class");
    assertThat(tree.name().text()).isEqualTo("A");
    assertThat(tree.extendsToken().text()).isEqualTo("extends");
    assertThat(tree.superClass().name().text()).isEqualTo("B");
    assertThat(tree.implementsToken().text()).isEqualTo("implements");
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.members()).hasSize(1);
  }

  @Test
  void simpleClassDeclaration() {
    ClassDeclarationTree tree = parse("class A { }", PHPLexicalGrammar.CLASS_DECLARATION);

    assertThat(tree.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree.modifiersToken()).isEmpty();
    assertThat(tree.extendsToken()).isNull();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.implementsToken()).isNull();
    assertThat(tree.superInterfaces()).isEmpty();
    assertThat(tree.members()).isEmpty();
  }

  @Test
  void traitDeclaration() {
    ClassDeclarationTree tree = parse("trait A { function foo(){} }", PHPLexicalGrammar.TRAIT_DECLARATION);

    assertThat(tree.is(Kind.TRAIT_DECLARATION)).isTrue();
    assertThat(tree.modifiersToken()).isEmpty();
    assertThat(tree.classToken().text()).isEqualTo("trait");
    assertThat(tree.name().text()).isEqualTo("A");
    assertThat(tree.extendsToken()).isNull();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.implementsToken()).isNull();
    assertThat(tree.superInterfaces()).isEmpty();
    assertThat(tree.members()).hasSize(1);
  }

  @Test
  void interfaceDeclaration() {
    ClassDeclarationTree tree = parse("interface A extends B, C { function foo(); }", PHPLexicalGrammar.INTERFACE_DECLARATION);

    assertThat(tree.is(Kind.INTERFACE_DECLARATION)).isTrue();
    assertThat(tree.modifiersToken()).isEmpty();
    assertThat(tree.classToken().text()).isEqualTo("interface");
    assertThat(tree.name().text()).isEqualTo("A");
    assertThat(tree.extendsToken()).isNotNull();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.implementsToken()).isNull();
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.members()).hasSize(1);
  }

  @Test
  void testFetchConstructor() {
    ClassDeclarationTree tree = parse("class A { function __construct(); }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree.fetchConstructor().name().text()).isEqualTo(ClassTree.PHP5_CONSTRUCTOR_NAME);

    tree = parse("class A { function A(); }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree.fetchConstructor().name().text()).isEqualTo("A");

    tree = parse("class A { function doSomething(); }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree.fetchConstructor()).isNull();
  }

  @Test
  void withAttributes() {
    ClassDeclarationTree tree = parse("#[A1,] class A {}", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
  }

  @Test
  void readonlyClassDeclaration() {
    ClassDeclarationTree tree1 = parse("readonly class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree1.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree1.modifiersToken()).hasSize(1);
    assertThat(tree1.modifiersToken().get(0).text()).isEqualTo("readonly");
    assertThat(tree1.members()).hasSize(1);

    ClassDeclarationTree tree2 = parse("abstract readonly class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree2.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree2.modifiersToken()).hasSize(2);
    assertThat(tree2.modifiersToken().get(1).text()).isEqualTo("readonly");
    assertThat(tree2.members()).hasSize(1);

    ClassDeclarationTree tree3 = parse("readonly abstract class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree3.is(Kind.CLASS_DECLARATION)).isTrue();
    assertThat(tree3.modifiersToken()).hasSize(2);
    assertThat(tree3.modifiersToken().get(0).text()).isEqualTo("readonly");
    assertThat(tree3.members()).hasSize(1);
  }

  @Test
  void classDeclarationTreeHelpers() {
    ClassDeclarationTree tree1 = parse("class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree1.isAbstract()).isFalse();
    assertThat(tree1.isFinal()).isFalse();
    assertThat(tree1.isReadOnly()).isFalse();
    assertThat(tree1.modifierToken()).isNull();

    ClassDeclarationTree tree2 = parse("abstract class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree2.isAbstract()).isTrue();
    assertThat(tree2.isFinal()).isFalse();
    assertThat(tree2.isReadOnly()).isFalse();
    assertThat(tree2.modifierToken().text()).isEqualTo("abstract");

    ClassDeclarationTree tree3 = parse("final class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree3.isAbstract()).isFalse();
    assertThat(tree3.isFinal()).isTrue();
    assertThat(tree3.isReadOnly()).isFalse();
    assertThat(tree3.modifierToken().text()).isEqualTo("final");

    ClassDeclarationTree tree4 = parse("readonly class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree4.isAbstract()).isFalse();
    assertThat(tree4.isFinal()).isFalse();
    assertThat(tree4.isReadOnly()).isTrue();
    assertThat(tree4.modifierToken()).isNull();

    ClassDeclarationTree tree5 = parse("abstract readonly class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree5.isAbstract()).isTrue();
    assertThat(tree5.isFinal()).isFalse();
    assertThat(tree5.isReadOnly()).isTrue();
    assertThat(tree5.modifierToken().text()).isEqualTo("abstract");

    ClassDeclarationTree tree6 = parse("final readonly class A { public $var; }", PHPLexicalGrammar.CLASS_DECLARATION);
    assertThat(tree6.isAbstract()).isFalse();
    assertThat(tree6.isFinal()).isTrue();
    assertThat(tree6.isReadOnly()).isTrue();
    assertThat(tree6.modifierToken().text()).isEqualTo("final");
  }
}
