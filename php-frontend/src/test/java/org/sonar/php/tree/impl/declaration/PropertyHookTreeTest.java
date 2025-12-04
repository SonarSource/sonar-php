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
import org.sonar.plugins.php.api.tree.declaration.PropertyHookTree;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyHookTreeTest extends PHPTreeModelTest {

  @Test
  void shouldParsePropertyHookMethodDeclaration() {
    PropertyHookTree tree = parse("#[A1(8), A2] final &get { return $this-> a+1; }", PHPLexicalGrammar.PROPERTY_HOOK);
    assertThat(tree.is(Tree.Kind.PROPERTY_HOOK_METHOD_DECLARATION)).isTrue();
    assertThat(((PropertyHookTreeImpl) tree).childrenIterator()).toIterable().hasSize(5);
    assertThat(tree.attributeGroups()).hasSize(1);
    assertThat(tree.attributeGroups().get(0).attributes()).hasSize(2);
    assertThat(tree.modifierToken()).isNotNull();
    assertThat(tree.referenceToken()).isNotNull();
    assertThat(tree.name().text()).isEqualTo("get");
    assertThat(tree.parameters()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(tree.body().is(Tree.Kind.BLOCK)).isTrue();
  }

  @Test
  void shouldParsePropertyHookMethodDeclarationWithDoubleArrow() {
    PropertyHookTree tree = parse("final set($value) => $value - 1;", PHPLexicalGrammar.PROPERTY_HOOK);
    assertThat(tree.is(Tree.Kind.PROPERTY_HOOK_METHOD_DECLARATION)).isTrue();
    assertThat(((PropertyHookTreeImpl) tree).childrenIterator()).toIterable().hasSize(5);
    assertThat(tree.attributeGroups()).isEmpty();
    assertThat(tree.modifierToken()).isNotNull();
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.name().text()).isEqualTo("set");
    assertThat(tree.parameters().parameters()).hasSize(1);
    assertThat(tree.parameters().parameters().get(0).variableIdentifier().text()).isEqualTo("$value");

    assertThat(tree.doubleArrowToken()).isNotNull();
    assertThat(tree.body().is(Tree.Kind.EXPRESSION_STATEMENT)).isTrue();
  }

  @Test
  void shouldParseAbstractPropertyHook() {
    PropertyHookTree tree = parse("get;", PHPLexicalGrammar.PROPERTY_HOOK);
    assertThat(tree.is(Tree.Kind.PROPERTY_HOOK_METHOD_DECLARATION)).isTrue();
    assertThat(((PropertyHookTreeImpl) tree).childrenIterator()).toIterable().hasSize(2);
    assertThat(tree.attributeGroups()).isEmpty();
    assertThat(tree.modifierToken()).isNull();
    assertThat(tree.referenceToken()).isNull();
    assertThat(tree.name().text()).isEqualTo("get");
    assertThat(tree.parameters()).isNull();

    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(tree.body().is(Tree.Kind.TOKEN)).isTrue();
  }
}
