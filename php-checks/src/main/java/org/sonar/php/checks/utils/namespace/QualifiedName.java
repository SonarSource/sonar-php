/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks.utils.namespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;

public class QualifiedName {
  private List<String> parentNamespaces;
  private String name;

  private QualifiedName(List<String> parentNamespaces, String name) {
    this.parentNamespaces = parentNamespaces;
    this.name = name;
  }

  public static QualifiedName create(String... names) {
    int length = names.length;
    if (length == 0) {
      throw new IllegalStateException("Cannot create an empty qualified name");
    } else if (length == 1) {
      return new QualifiedName(Collections.emptyList(), names[0]);
    } else {
      List<String> parents = Arrays.asList(names).subList(0, length - 1);
      return new QualifiedName(parents, names[length - 1]);
    }
  }

  public static QualifiedName create(NamespaceNameTree nameTree) {
    List<String> namespaces = nameTree.namespaces().stream().map(NameIdentifierTree::text).collect(Collectors.toList());
    return new QualifiedName(namespaces, nameTree.name().text());
  }

  public static QualifiedName create(QualifiedName parentNamespace, QualifiedName nameInNamespace) {
    List<String> parentNamespaces = parentNamespace.asList();
    parentNamespaces.addAll(nameInNamespace.parentNamespaces);
    return new QualifiedName(parentNamespaces, nameInNamespace.name);
  }

  public List<String> asList() {
    List<String> list = new ArrayList<>(parentNamespaces);
    list.add(name);
    return list;
  }

  public String firstPart() {
    if (!parentNamespaces.isEmpty()) {
      return parentNamespaces.get(0);
    }
    return name;
  }

  public QualifiedName withOriginalName(QualifiedName originalName) {
    if (parentNamespaces.isEmpty()) {
      // 'this.name' is the alias for the original name
      return originalName;
    } else {
      List<String> parents = originalName.asList();
      // element at index 0 of parentNamespaces is the alias for the original name
      parentNamespaces.stream().skip(1L).forEach(parents::add);
      return new QualifiedName(parents, name);
    }
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
    return Objects.equals(parentNamespaces, that.parentNamespaces) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentNamespaces, name);
  }

  @Override
  public String toString() {
    return asList().toString();
  }
}
