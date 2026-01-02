/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class NamespaceNameTreeImpl extends PHPTree implements NamespaceNameTree {

  private static final Kind KIND = Kind.NAMESPACE_NAME;

  private final SyntaxToken absoluteSeparator;
  private final SeparatedList<NameIdentifierTree> namespaces;
  private final IdentifierTree name;

  private final String fullName;
  private final String qualifiedName;

  public NamespaceNameTreeImpl(@Nullable SyntaxToken absoluteSeparator, SeparatedList<NameIdentifierTree> namespaces, IdentifierTree name) {
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
  public SeparatedList<NameIdentifierTree> namespaces() {
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
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(absoluteSeparator),
      namespaces.elementsAndSeparators(),
      IteratorUtils.iteratorOf(name));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitNamespaceName(this);
  }
}
