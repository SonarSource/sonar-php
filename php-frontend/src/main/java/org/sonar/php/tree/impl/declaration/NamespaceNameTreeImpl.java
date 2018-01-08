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
package org.sonar.php.tree.impl.declaration;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class NamespaceNameTreeImpl extends PHPTree implements NamespaceNameTree {

  private static final Kind KIND = Kind.NAMESPACE_NAME;

  private final SyntaxToken absoluteSeparator;
  private final SeparatedListImpl<NameIdentifierTree> namespaces;
  private final IdentifierTree name;

  private final String fullName;
  private final String qualifiedName;

  public NamespaceNameTreeImpl(@Nullable InternalSyntaxToken absoluteSeparator, SeparatedListImpl<NameIdentifierTree> namespaces, IdentifierTree name) {
    this.absoluteSeparator = absoluteSeparator;
    this.namespaces = namespaces;
    this.name = name;
    this.fullName = getFullName();
    this.qualifiedName = new StringBuilder()
      .append(qualifiers())
      .append(unqualifiedName()).toString();
  }

  @Nullable
  @Override
  public SyntaxToken absoluteSeparator() {
    return this.absoluteSeparator;
  }

  @Override
  public SeparatedListImpl<NameIdentifierTree> namespaces() {
    return namespaces;
  }

  @Override
  public IdentifierTree name() {
    return name;
  }

  @Override
  public String fullName() {
    return fullName;
  }

  @Override
  public String unqualifiedName() {
    return name().text();
  }

  @Override
  public String qualifiedName() {
    return qualifiedName;
  }

  @Override
  public String fullyQualifiedName() {
    return getFullName();
  }

  @Override
  public boolean isFullyQualified() {
    return absoluteSeparator != null;
  }

  @Override
  public boolean hasQualifiers() {
    return !namespaces().isEmpty();
  }

  private String getFullName() {
    StringBuilder result = new StringBuilder();

    if (absoluteSeparator != null) {
      result.append(absoluteSeparator.text());
    }
    result.append(qualifiers());
    result.append(unqualifiedName());

    return result.toString();
  }

  private String qualifiers() {
    StringBuilder result = new StringBuilder();

    Iterator<Tree> iterator = this.namespaces.elementsAndSeparators();
    while (iterator.hasNext()) {
      Tree next = iterator.next();
      if (next.is(Kind.NAME_IDENTIFIER)) {
        result.append(((NameIdentifierTree) next).text());
      } else {
        result.append(((SyntaxToken) next).text());
      }
    }
    return result.toString();
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.singletonIterator(absoluteSeparator),
        namespaces.elementsAndSeparators(),
        Iterators.singletonIterator(name)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitNamespaceName(this);
  }
}
