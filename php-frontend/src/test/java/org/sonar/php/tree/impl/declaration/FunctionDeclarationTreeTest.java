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

import com.sonar.sslr.api.RecognitionException;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FunctionDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void simpleDeclaration() {
    FunctionDeclarationTree tree = parse("function f($p) {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.is(Kind.FUNCTION_DECLARATION)).isTrue();
    assertThat(tree.attributeGroups()).isEmpty();
    assertThat(tree.functionToken().text()).isEqualTo("function");
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.name().text()).isEqualTo("f");
    assertThat(tree.parameters().parameters()).hasSize(1);
    assertThat(tree.returnTypeClause()).isNull();
    assertThat(tree.body().statements()).isEmpty();
  }

  @Test
  void reference() {
    FunctionDeclarationTree tree = parse("function &f($p) {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.referenceToken()).isNotNull();
  }

  @Test
  void withReturnTypeClause() {
    FunctionDeclarationTree tree = parse("function f() : array {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.returnTypeClause()).isNotNull();
    assertThat(tree.returnTypeClause().colonToken().text()).isEqualTo(":");
    assertThat(tree.returnTypeClause().type().typeName().toString()).hasToString("array");
    assertThat(tree.returnTypeClause().declaredType().isSimple()).isTrue();
    assertThat(((TypeTree) tree.returnTypeClause().declaredType()).typeName().is(Kind.BUILT_IN_TYPE)).isTrue();
  }

  @Test
  void withUnionReturnTypeClause() {
    FunctionDeclarationTree tree = parse("function f() : array|int {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.returnTypeClause()).isNotNull();
    assertThat(tree.returnTypeClause().colonToken().text()).isEqualTo(":");
    assertThat(tree.returnTypeClause().type().typeName()).hasToString("array");
    assertThat(tree.returnTypeClause().declaredType().isSimple()).isFalse();
    assertThat(((UnionTypeTree) tree.returnTypeClause().declaredType()).types()).hasSize(2);
  }

  @Test
  void withAttributes() {
    FunctionDeclarationTree tree = parse("#[A1(8), A2] function f() {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(2);
  }

  @Test
  void parameterWithVisibilityModifier() {
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> parse("function f(public $p) {}", PHPLexicalGrammar.FUNCTION_DECLARATION));
  }
}
