/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.Assertions;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;

import static org.assertj.core.api.Assertions.assertThat;

class AnonymousClassTreeTest extends PHPTreeModelTest {
  @Test
  void shouldParseAnonymousClassDeclarations() {
    Assertions.assertThat(Kind.ANONYMOUS_CLASS)
      .matches("class (1, foo()) extends A implements B, C {var $a;}")
      .matches("class (name: 'a') {}")
      .matches("readonly class {}")
      .matches("#[A1,] readonly class {}")

      .notMatches("readonly #[A1,] class {}")
      .notMatches("class readonly {}");
  }

  @Test
  void test() throws Exception {
    AnonymousClassTree tree = parse("class (1, foo()) extends A implements B, C {var $a;}", Kind.ANONYMOUS_CLASS);

    assertThat(tree.is(Kind.ANONYMOUS_CLASS)).isTrue();
    assertThat(tree.classToken().text()).isEqualTo("class");
    assertThat(tree.openCurlyBraceToken()).isNotNull();
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.closeCurlyBraceToken()).isNotNull();
    assertThat(tree.extendsToken()).isNotNull();
    assertThat(tree.superClass().fullName()).isEqualTo("A");
    assertThat(tree.implementsToken()).isNotNull();
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.members()).hasSize(1);
  }

  @Test
  void test_with_named_arguments() {
    AnonymousClassTree tree = parse("class (name: 'a') {}", Kind.ANONYMOUS_CLASS);
    assertThat(tree.callArguments()).hasSize(1);
  }

  @Test
  void with_attributes() {
    AnonymousClassTree tree = parse("#[A1,] class {}", Kind.ANONYMOUS_CLASS);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(1);
  }

  @Test
  void with_readonly() {
    AnonymousClassTree tree = parse("readonly class {}", Kind.ANONYMOUS_CLASS);
    assertThat(tree.readonlyToken()).isNotNull();
    assertThat(((PHPTree) tree).childrenIterator()).hasSize(9);
  }
}
