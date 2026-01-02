/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterListTreeTest extends PHPTreeModelTest {

  @Test
  void empty() {
    ParameterListTree tree = parameterList("()");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.parameters()).isEmpty();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
  }

  @Test
  void notEmpty() {
    assertThat(parameterList("($p1)").parameters()).hasSize(1);
    assertThat(parameterList("($p1, $p2)").parameters()).hasSize(2);
    assertThat(parameterList("($p1, $p2,)").parameters()).hasSize(2);
  }

  @Test
  void withAttributes() {
    assertThat(parameterList("(#[A1(5)] $p1, #[A1(6)] $p2)").parameters()).hasSize(2);
  }

  private ParameterListTree parameterList(String toParse) {
    ParameterListTree tree = parse(toParse, PHPLexicalGrammar.PARAMETER_LIST);
    assertThat(tree.is(Kind.PARAMETER_LIST)).isTrue();
    return tree;
  }

  @Test
  void shouldSupportPropertyHooks() {
    ParameterListTree tree = parameterList("(int $a { get; set => 123; }, $b)");
    assertThat(tree.is(Kind.PARAMETER_LIST)).isTrue();
    assertThat(((ParameterListTreeImpl) tree).childrenIterator()).toIterable().hasSize(5);
    assertThat(tree.parameters()).hasSize(2);
    assertThat((tree.parameters().get(0).propertyHookList())).isNotNull();
    assertThat((tree.parameters().get(0).propertyHookList().hooks())).hasSize(2);
  }
}
