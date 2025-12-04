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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

import static org.assertj.core.api.Assertions.assertThat;

class NamespaceNameTreeTest extends PHPTreeModelTest {

  @Test
  void simpleAbsoluteNamespace() {
    NamespaceNameTree tree = parse("\\a", PHPLexicalGrammar.NAMESPACE_NAME);
    assertThat(tree.namespaces()).isEmpty();
    assertThat(tree.name().text()).isEqualTo("a");
  }

  @Test
  void absoluteNamespace() {
    NamespaceNameTree tree = parse("\\ns1\\ns2\\name", PHPLexicalGrammar.NAMESPACE_NAME);

    assertThat(tree.is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.absoluteSeparator()).isNotNull();
    assertThat(tree.name().text()).isEqualTo("name");
    assertThat(tree.namespaces()).hasSize(2);
    assertThat(tree.fullName()).isEqualTo("\\ns1\\ns2\\name");
  }

  @Test
  void relativeNamespace() {
    NamespaceNameTree tree = parse("ns1\\ns2\\name", PHPLexicalGrammar.NAMESPACE_NAME);

    assertThat(tree.is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.absoluteSeparator()).isNull();
    assertThat(tree.name().text()).isEqualTo("name");
    assertThat(tree.namespaces()).hasSize(2);
    assertThat(tree.fullName()).isEqualTo("ns1\\ns2\\name");
  }

  @Test
  void relativeNamespaceWithKeyword() {
    NamespaceNameTree tree = parse("foo\\while\\if", PHPLexicalGrammar.NAMESPACE_NAME);
    assertThat(tree.namespaces()).hasSize(2);
    assertThat(tree.name().text()).isEqualTo("if");
  }

  @Test
  void absoluteNamespaceWithKeyword() {
    NamespaceNameTree tree = parse("\\for\\while\\if", PHPLexicalGrammar.NAMESPACE_NAME);
    assertThat(tree.namespaces()).hasSize(2);
    assertThat(tree.name().text()).isEqualTo("if");
  }
}
