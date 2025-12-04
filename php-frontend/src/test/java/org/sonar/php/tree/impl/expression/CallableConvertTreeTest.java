/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

class CallableConvertTreeTest extends PHPTreeModelTest {

  @Test
  void simpleCallableConvert() {
    CallableConvertTreeImpl tree = parse("f(...)", PHPLexicalGrammar.EXPRESSION);
    assertThat(tree.is(Tree.Kind.CALLABLE_CONVERT)).isTrue();
    assertThat(tree.childrenIterator()).toIterable().hasSize(4);
    assertThat(tree.expression().is(Tree.Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.expression()).hasToString("f");
    assertThat(tree.openParenthesisToken()).hasToString("(");
    assertThat(tree.ellipsisToken()).hasToString("...");
    assertThat(tree.closeParenthesisToken()).hasToString(")");
  }
}
