/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.symbols;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

public class SymbolQualifiedName implements QualifiedName {

  static final SymbolQualifiedName GLOBAL_NAMESPACE = new SymbolQualifiedName(ImmutableList.of());

  private final ImmutableList<String> nameElements;

  private SymbolQualifiedName(List<String> parentNamespaces, String name) {
    this(ImmutableList.<String>builder()
      .addAll(parentNamespaces)
      .add(name)
      .build());
  }

  private SymbolQualifiedName(List<String> nameElements) {
    ImmutableList.Builder<String> nameBuilder = ImmutableList.builder();
    nameElements.forEach(name -> nameBuilder.add(name.toLowerCase(Locale.ROOT)));
    this.nameElements = nameBuilder.build();
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
      return new SymbolQualifiedName(ImmutableList.copyOf(names));
    }
  }

  public static SymbolQualifiedName create(NamespaceNameTree nameTree) {
    List<String> namespaces = nameTree.namespaces().stream().map(NameIdentifierTree::text).collect(Collectors.toList());
    return new SymbolQualifiedName(namespaces, nameTree.name().text());
  }

  SymbolQualifiedName resolve(SymbolQualifiedName nameInNamespace) {
    ImmutableList<String> newName = ImmutableList.<String>builder()
      .addAll(this.nameElements)
      .addAll(nameInNamespace.nameElements)
      .build();
    return new SymbolQualifiedName(newName);
  }

  /**
   * Used to resolve namespaceNameTree which is using an alias
   */
  SymbolQualifiedName resolveAliasedName(NamespaceNameTree namespaceNameTree) {
    if (namespaceNameTree.namespaces().isEmpty()) {
      throw new IllegalStateException("Unable to resolve " + namespaceNameTree + " which has only aliased name");
    }
    ImmutableList.Builder<String> nameBuilder = ImmutableList.<String>builder()
      .addAll(nameElements);
    // skip the first element of the namespace, because it's an alias
    namespaceNameTree.namespaces()
      .stream()
      .skip(1)
      .map(NameIdentifierTree::text)
      .forEach(nameBuilder::add);
    nameBuilder.add(namespaceNameTree.name().text());
    return new SymbolQualifiedName(nameBuilder.build());
  }

  SymbolQualifiedName resolve(String name) {
    return new SymbolQualifiedName(nameElements, name);
  }

  @Override
  public String simpleName() {
    return Iterables.getLast(nameElements);
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
