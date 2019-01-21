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
package org.sonar.plugins.php.api.symbols;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

public class QualifiedName {

  public static final QualifiedName GLOBAL_NAMESPACE = new QualifiedName(ImmutableList.of());

  private final ImmutableList<String> nameElements;

  private QualifiedName(List<String> parentNamespaces, String name) {
    this.nameElements = ImmutableList.<String>builder()
      .addAll(parentNamespaces)
      .add(name)
      .build();
  }

  private QualifiedName(List<String> nameElements) {
    this.nameElements = ImmutableList.copyOf(nameElements);
  }

  /**
   * Utility method to conveniently create QualifiedName objects with PHP namespace notation.
   * Ex: qualifiedName("Foo\Bar\FooBar")
   */
  public static QualifiedName qualifiedName(String qualifiedNameString) {
    String qn = qualifiedNameString.startsWith("\\") ? qualifiedNameString.substring(1) : qualifiedNameString;
    return create(qn.split("\\\\"));
  }

  public static QualifiedName create(String... names) {
    int length = names.length;
    if (length == 0) {
      throw new IllegalStateException("Cannot create an empty qualified name");
    } else if (length == 1) {
      return new QualifiedName(Collections.emptyList(), names[0]);
    } else {
      return new QualifiedName(Arrays.asList(names));
    }
  }

  public static QualifiedName create(NamespaceNameTree nameTree) {
    List<String> namespaces = nameTree.namespaces().stream().map(NameIdentifierTree::text).collect(Collectors.toList());
    return new QualifiedName(namespaces, nameTree.name().text());
  }

  public QualifiedName resolve(QualifiedName nameInNamespace) {
    ImmutableList<String> newName = ImmutableList.<String>builder()
      .addAll(this.nameElements)
      .addAll(nameInNamespace.nameElements)
      .build();
    return new QualifiedName(newName);
  }

  public QualifiedName resolve(String name) {
    return new QualifiedName(nameElements, name);
  }

  public boolean isGlobal() {
    return this == GLOBAL_NAMESPACE;
  }

  String name() {
    return Iterables.getLast(nameElements);
  }

  public boolean equalsIgnoreCase(QualifiedName other) {
    if (this == other) {
      return true;
    }
    if (this.nameElements.size() != other.nameElements.size()) {
      return false;
    }
    for (int i = 0; i < this.nameElements.size(); i++) {
      if (!this.nameElements.get(i).equalsIgnoreCase(other.nameElements.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QualifiedName)) {
      return false;
    }
    QualifiedName that = (QualifiedName) o;
    return Objects.equals(nameElements, that.nameElements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nameElements);
  }

  @Override
  public String toString() {
    return nameElements.stream().collect(Collectors.joining("\\", "\\", ""));
  }
}
