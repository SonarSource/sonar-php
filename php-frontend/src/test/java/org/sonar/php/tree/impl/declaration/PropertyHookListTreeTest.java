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

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookListTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookTree;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyHookListTreeTest extends PHPTreeModelTest {

  @Test
  void shouldParsePropertyHookList() {
    PropertyHookListTree tree = parse("{ #[A1(8), A2] final &get { return $this-> a+1; } }", PHPLexicalGrammar.PROPERTY_HOOK_LIST);
    assertThat(tree.is(Tree.Kind.PROPERTY_HOOK_LIST)).isTrue();
    assertThat(tree.openCurlyBrace()).isNotNull();
    assertThat(tree.closeCurlyBrace()).isNotNull();
    assertThat(tree.hooks()).hasSize(1);
    assertThat(((PropertyHookListTreeImpl) tree).childrenIterator()).toIterable().hasSize(3);

    PropertyHookTree firstPropertyHook = tree.hooks().get(0);
    assertThat(firstPropertyHook.attributeGroups()).hasSize(1);
    assertThat(firstPropertyHook.attributeGroups().get(0).attributes()).hasSize(2);
    assertThat(firstPropertyHook.modifierToken()).isNotNull();
    assertThat(firstPropertyHook.referenceToken()).isNotNull();
    assertThat(firstPropertyHook.name().text()).isEqualTo("get");
    assertThat(firstPropertyHook.parameters()).isNull();
    assertThat(firstPropertyHook.doubleArrowToken()).isNull();
    assertThat(firstPropertyHook.body().is(Tree.Kind.BLOCK)).isTrue();
  }

  @Test
  void shouldParsePropertyHookListWithDoubleArrow() {
    PropertyHookListTree tree = parse("{ final set($value) => $value - 1; }", PHPLexicalGrammar.PROPERTY_HOOK_LIST);
    assertThat(tree.is(Tree.Kind.PROPERTY_HOOK_LIST)).isTrue();
    assertThat(tree.openCurlyBrace()).isNotNull();
    assertThat(tree.closeCurlyBrace()).isNotNull();
    assertThat(tree.hooks()).hasSize(1);
    assertThat(((PropertyHookListTreeImpl) tree).childrenIterator()).toIterable().hasSize(3);

    PropertyHookTree firstPropertyHook = tree.hooks().get(0);
    assertThat(firstPropertyHook.attributeGroups()).isEmpty();
    assertThat(firstPropertyHook.modifierToken()).isNotNull();
    assertThat(firstPropertyHook.referenceToken()).isNull();
    assertThat(firstPropertyHook.name().text()).isEqualTo("set");
    assertThat(firstPropertyHook.parameters().parameters()).hasSize(1);
    assertThat(firstPropertyHook.parameters().parameters().get(0).variableIdentifier().text()).isEqualTo("$value");

    assertThat(firstPropertyHook.doubleArrowToken()).isNotNull();
    assertThat(firstPropertyHook.body().is(Tree.Kind.EXPRESSION_STATEMENT)).isTrue();
  }

  @Test
  void shouldParsePropertyHookListWithMultiplePropertyHooks() {
    PropertyHookListTree tree = parse("{ get; &set; }", PHPLexicalGrammar.PROPERTY_HOOK_LIST);
    assertThat(tree.is(Tree.Kind.PROPERTY_HOOK_LIST)).isTrue();
    assertThat(tree.openCurlyBrace()).isNotNull();
    assertThat(tree.closeCurlyBrace()).isNotNull();
    assertThat(tree.hooks()).hasSize(2);
    assertThat(((PropertyHookListTreeImpl) tree).childrenIterator()).toIterable().hasSize(4);
  }
}
