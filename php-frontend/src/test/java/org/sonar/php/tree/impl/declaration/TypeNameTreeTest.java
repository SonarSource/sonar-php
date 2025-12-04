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
package org.sonar.php.tree.impl.declaration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

import static org.assertj.core.api.Assertions.assertThat;

class TypeNameTreeTest extends PHPTreeModelTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "int", "mixed", "Int", "callable"
  })
  void builtInType(String typeName) {
    BuiltInTypeTree tree = parse(typeName, PHPLexicalGrammar.TYPE_NAME);

    assertThat(tree.is(Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(tree.token().text()).isEqualTo(typeName);
  }

  @Test
  void namespaceNameType() {
    NamespaceNameTree tree = parse("MyClass", PHPLexicalGrammar.TYPE_NAME);

    assertThat(tree.is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.fullName()).isEqualTo("MyClass");
  }
}
