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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.php.utils.collections.ListUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

import static org.sonar.php.utils.collections.ListUtils.getLast;

public class SymbolQualifiedName implements QualifiedName {

  static final SymbolQualifiedName GLOBAL_NAMESPACE = new SymbolQualifiedName(Collections.emptyList());

  private final List<String> nameElements;

  private SymbolQualifiedName(List<String> parentNamespaces, String name) {
    this(ListUtils.concat(parentNamespaces, Collections.singletonList(name)));
  }

  private SymbolQualifiedName(List<String> nameElements) {
    this.nameElements = nameElements.stream().map(name -> name.toLowerCase(Locale.ROOT)).toList();
  }

  /**
   * Utility method to conveniently create SymbolQualifiedName objects with PHP namespace notation.
   * Ex: qualifiedName("Foo\Bar\FooBar")
   */
  public static SymbolQualifiedName qualifiedName(String qualifiedNameString) {
    String qn = qualifiedNameString.startsWith("\\") ? qualifiedNameString.substring(1) : qualifiedNameString;
    return create(qn.split("\\\\"));
  }

  public static SymbolQualifiedName create(String... names) {
    int length = names.length;
    if (length == 0) {
      throw new IllegalStateException("Cannot create an empty qualified name");
    } else {
      return new SymbolQualifiedName(Arrays.asList(names));
    }
  }

  public static SymbolQualifiedName create(NamespaceNameTree nameTree) {
    List<String> namespaces = nameTree.namespaces().stream().map(NameIdentifierTree::text).toList();
    return new SymbolQualifiedName(namespaces, nameTree.name().text());
  }

  SymbolQualifiedName resolve(SymbolQualifiedName nameInNamespace) {
    List<String> newName = ListUtils.concat(this.nameElements, nameInNamespace.nameElements);
    return new SymbolQualifiedName(newName);
  }

  /**
   * Used to resolve namespaceNameTree which is using an alias
   */
  SymbolQualifiedName resolveAliasedName(NamespaceNameTree namespaceNameTree) {
    if (namespaceNameTree.namespaces().isEmpty()) {
      throw new IllegalStateException("Unable to resolve " + namespaceNameTree + " which has only aliased name");
    }
    List<String> nameElements = new ArrayList<>(this.nameElements);
    // skip the first element of the namespace, because it's an alias
    namespaceNameTree.namespaces()
      .stream()
      .skip(1)
      .map(NameIdentifierTree::text)
      .forEach(nameElements::add);
    nameElements.add(namespaceNameTree.name().text());
    return new SymbolQualifiedName(nameElements);
  }

  SymbolQualifiedName resolve(String name) {
    return new SymbolQualifiedName(nameElements, name);
  }

  @Override
  public String simpleName() {
    return getLast(nameElements);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SymbolQualifiedName)) {
      return false;
    }
    SymbolQualifiedName that = (SymbolQualifiedName) o;
    return Objects.equals(nameElements, that.nameElements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nameElements);
  }

  @Override
  public String toString() {
    return nameElements.stream().collect(Collectors.joining("\\"));
  }
}
