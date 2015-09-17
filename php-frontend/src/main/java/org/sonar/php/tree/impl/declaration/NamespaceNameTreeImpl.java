/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.declaration;

import com.google.common.base.Functions;
import com.google.common.collect.Iterators;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import javax.annotation.Nullable;
import java.util.Iterator;

public class NamespaceNameTreeImpl extends PHPTree implements NamespaceNameTree {

  private static final Kind KIND = Kind.NAMESPACED_NAME;

  private final SyntaxToken absoluteSeparator;
  private final SeparatedList<IdentifierTree> namespaces;
  private final IdentifierTree name;

  private final String fullName;

  public NamespaceNameTreeImpl(@Nullable InternalSyntaxToken absoluteSeparator, SeparatedList<IdentifierTree> namespaces, IdentifierTree name) {
    this.absoluteSeparator = absoluteSeparator;
    this.namespaces = namespaces;
    this.name = name;
    this.fullName = getFullName();
  }

  @Nullable
  @Override
  public SyntaxToken absoluteSeparator() {
    return this.absoluteSeparator;
  }

  @Override
  public SeparatedList<IdentifierTree> namespaces() {
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

  private String getFullName() {
    String separator = PHPPunctuator.NS_SEPARATOR.getValue();
    StringBuilder result = new StringBuilder();

    if (absoluteSeparator != null) {
      result.append(separator);
    }

    Iterator<Tree> iterator = this.namespaces.elementsAndSeparators(Functions.<IdentifierTree>identity());
    while (iterator.hasNext()) {
      Tree next = iterator.next();
      if (next.is(Kind.IDENTIFIER)) {
        result.append(((IdentifierTree) next).name());
      } else {
        result.append(separator);
      }
    }
    result.append(name().name());
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
        namespaces.elementsAndSeparators(Functions.<IdentifierTree>identity()),
        Iterators.singletonIterator(name)
    );
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitNamespaceName(this);
  }
}
