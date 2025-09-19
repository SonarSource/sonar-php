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
package org.sonar.php.tree.symbols;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.NameIdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class SymbolQualifiedNameTest {

  @Test
  void shouldVerifyToStringAndEquals() {
    var qualifiedName1 = SymbolQualifiedName.create("A", "B", "C");
    var qualifiedName2 = SymbolQualifiedName.create("a", "b", "c");
    assertThat(qualifiedName1)
      .hasToString("a\\b\\c")
      .isEqualTo(qualifiedName2);
  }

  @Test
  void shouldThrowExceptionForCreateAndZeroArguments() {
    assertThatException().isThrownBy(SymbolQualifiedName::create)
      .isInstanceOf(IllegalStateException.class)
      .withMessage("Cannot create an empty qualified name");
  }

  @Test
  void shouldThrowExceptionForEmptyNamespace() {
    var qualifiedName = SymbolQualifiedName.create("A", "B", "C");
    var token = new InternalSyntaxToken(1,2,"my_name", List.of(), 0, false);
    var nameIdTree = new NameIdentifierTreeImpl(token);
    var namespaces = new SeparatedListImpl<NameIdentifierTree>(List.of(), List.of());
    var namespaceNameTree = new NamespaceNameTreeImpl(null, namespaces, nameIdTree);
    assertThatException().isThrownBy(() -> qualifiedName.resolveAliasedName(namespaceNameTree))
      .isInstanceOf(IllegalStateException.class)
      .withMessage("Unable to resolve my_name which has only aliased name");
  }
  
  @Test
  void shouldReturnFalseForEqualsForDifferentTypes() {
    var qualifiedName = SymbolQualifiedName.create("A", "B", "C");
    var memberQualifiedName = new MemberQualifiedName(null, "my_name", null);
    var actual = qualifiedName.equals(memberQualifiedName);
    assertThat(actual).isFalse();
  }

  @Test
  void shouldReturnTruForEqualsWhenCompareToItself() {
    var qualifiedName = SymbolQualifiedName.create("A", "B", "C");
    var actual = qualifiedName.equals(qualifiedName);
    assertThat(actual).isTrue();
  }
}
