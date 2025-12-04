/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.declaration;

import com.sonar.sslr.api.RecognitionException;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.DnfTypeTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FunctionDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void shouldSupportSimpleDeclaration() {
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
  void shouldSupportReference() {
    FunctionDeclarationTree tree = parse("function &f($p) {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.referenceToken()).isNotNull();
  }

  @Test
  void shouldSupportReturnTypeClause() {
    FunctionDeclarationTree tree = parse("function f() : array {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.returnTypeClause()).isNotNull();
    assertThat(tree.returnTypeClause().colonToken().text()).isEqualTo(":");
    assertThat(tree.returnTypeClause().type().typeName().toString()).hasToString("array");
    assertThat(tree.returnTypeClause().declaredType().isSimple()).isTrue();
    assertThat(((TypeTree) tree.returnTypeClause().declaredType()).typeName().is(Kind.BUILT_IN_TYPE)).isTrue();
  }

  @Test
  void shouldSupportUnionReturnTypeClause() {
    FunctionDeclarationTree tree = parse("function f() : array|int {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.returnTypeClause()).isNotNull();
    assertThat(tree.returnTypeClause().colonToken().text()).isEqualTo(":");
    assertThat(tree.returnTypeClause().type().typeName()).hasToString("array");
    assertThat(tree.returnTypeClause().declaredType().isSimple()).isFalse();
    assertThat(((UnionTypeTree) tree.returnTypeClause().declaredType()).types()).hasSize(2);
  }

  @Test
  void shouldSupportAttributes() {
    FunctionDeclarationTree tree = parse("#[A1(8), A2] function f() {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(2);
  }

  @Test
  void shouldSupportParameterWithVisibilityModifier() {
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> parse("function f(public $p) {}", PHPLexicalGrammar.FUNCTION_DECLARATION));
  }

  @Test
  void shouldSupportDnfTypeInParameter() {
    FunctionDeclarationTree tree = parse("function f(int|null|(A&B) $p) {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.parameters().parameters()).hasSize(1);
    var type = tree.parameters().parameters().get(0).declaredType();
    assertThat(type.getKind()).isEqualTo(Kind.DNF_TYPE);
    var dnfType = (DnfTypeTree) type;
    assertThat(dnfType.isSimple()).isFalse();
    assertThat(dnfType.types()).hasSize(3);
  }

  @Test
  void shouldSupportPropertyHooks() {
    FunctionDeclarationTree tree = parse("function f(int $a { get; set => 123; }, $b) {}", PHPLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.is(Kind.FUNCTION_DECLARATION)).isTrue();
    assertThat(((FunctionDeclarationTreeImpl) tree).childrenIterator()).toIterable().hasSize(6);
    assertThat(tree.parameters().parameters()).hasSize(2);
    var propertyHooks = tree.parameters().parameters().get(0).propertyHookList();
    assertThat(propertyHooks.getKind()).isEqualTo(Kind.PROPERTY_HOOK_LIST);
    assertThat(propertyHooks.hooks()).hasSize(2);
  }
}
