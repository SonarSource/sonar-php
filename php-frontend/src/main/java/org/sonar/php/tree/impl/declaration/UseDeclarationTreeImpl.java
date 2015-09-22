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

import java.util.Iterator;

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.collect.Iterators;

public class UseDeclarationTreeImpl extends PHPTree implements UseDeclarationTree {

  private static final Kind KIND = Kind.USE_DECLARATION;
  private final NamespaceNameTree namespaceName;
  private final InternalSyntaxToken asToken;
  private final IdentifierTree alias;

  public UseDeclarationTreeImpl(NamespaceNameTree namespaceName, InternalSyntaxToken asToken, IdentifierTree alias) {
    this.namespaceName = namespaceName;
    this.asToken = asToken;
    this.alias = alias;
  }

  public UseDeclarationTreeImpl(NamespaceNameTree namespaceName) {
    this.namespaceName = namespaceName;
    this.asToken = null;
    this.alias = null;
  }

  @Override
  public ExpressionTree namespaceName() {
    return namespaceName;
  }

  @Nullable
  @Override
  public SyntaxToken asToken() {
    return asToken;
  }

  @Nullable
  @Override
  public IdentifierTree alias() {
    return alias;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(namespaceName, asToken, alias);
  }
  
  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitUseDeclaration(this);
  }

}
