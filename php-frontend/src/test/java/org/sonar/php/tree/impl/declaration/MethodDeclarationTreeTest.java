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
package org.sonar.php.tree.impl.declaration;

import com.sonar.sslr.api.RecognitionException;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MethodDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void test() {
    MethodDeclarationTree tree = parse("public final function &f($p) {}", PHPLexicalGrammar.METHOD_DECLARATION);
    assertThat(tree.is(Kind.METHOD_DECLARATION)).isTrue();
    assertThat(tree.modifiers()).hasSize(2);
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken()).isNotNull();
    assertThat(tree.name().text()).isEqualTo("f");
    assertThat(tree.parameters().parameters()).hasSize(1);
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(tree.body().is(Kind.BLOCK)).isTrue();
  }

  @Test
  void returnType() {
    MethodDeclarationTree tree = parse("public function f() : bool {}", PHPLexicalGrammar.METHOD_DECLARATION);
    assertThat(tree.returnTypeClause()).isNotNull();
  }

  @Test
  void returnTypeUnion() {
    MethodDeclarationTree tree = parse("public function f() : bool|array {}", PHPLexicalGrammar.METHOD_DECLARATION);
    assertThat(tree.returnTypeClause()).isNotNull();
    assertThat(tree.returnTypeClause().declaredType().isSimple()).isFalse();
  }

  @Test
  void withAttributes() {
    MethodDeclarationTree tree = parse("#[A1(8), A2] public static function f() {}", PHPLexicalGrammar.METHOD_DECLARATION);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(2);
  }

  @Test
  void constructorPropertyPromotion() {
    MethodDeclarationTree tree = parse("public function __construct(public $p) {}", PHPLexicalGrammar.METHOD_DECLARATION);
    assertThat(tree.parameters().parameters().get(0).visibility().text()).isEqualTo("public");

    tree = parse("public function __CONSTRUCT(private $p) {}", PHPLexicalGrammar.METHOD_DECLARATION);
    assertThat(tree.parameters().parameters().get(0).visibility().text()).isEqualTo("private");
  }

  @Test
  void nonConstructorParameterWithVisibilityModifier() {
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> parse("public function nonConstructor(public $p) {}", PHPLexicalGrammar.METHOD_DECLARATION));
  }
}
