/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.tree.impl.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;

import static org.assertj.core.api.Assertions.assertThat;

class TypeTreeTest extends PHPTreeModelTest {

  @Test
  void nonOptional() {
    TypeTree tree = parse("int", PHPLexicalGrammar.TYPE);

    assertThat(tree.is(Kind.TYPE)).isTrue();
    assertThat(tree.questionMarkToken()).isNull();
    assertThat(tree.typeName().is(Kind.BUILT_IN_TYPE)).isTrue();
  }

  @Test
  void optional() {
    TypeTree tree = parse("?MyClass", PHPLexicalGrammar.TYPE);

    assertThat(tree.is(Kind.TYPE)).isTrue();
    assertThat(tree.questionMarkToken().text()).isEqualTo("?");
    assertThat(tree.typeName().is(Kind.NAMESPACE_NAME)).isTrue();
  }

}
