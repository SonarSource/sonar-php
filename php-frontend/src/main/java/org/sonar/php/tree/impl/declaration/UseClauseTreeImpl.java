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
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class UseClauseTreeImpl extends PHPTree implements UseClauseTree {

  private static final Kind KIND = Kind.USE_CLAUSE;
  private final InternalSyntaxToken useTypeToken;
  private final NamespaceNameTree namespaceName;
  private final InternalSyntaxToken asToken;
  private final NameIdentifierTree alias;

  public UseClauseTreeImpl(@Nullable InternalSyntaxToken useTypeToken, NamespaceNameTree namespaceName, InternalSyntaxToken asToken, NameIdentifierTree alias) {
    this.useTypeToken = useTypeToken;
    this.namespaceName = namespaceName;
    this.asToken = asToken;
    this.alias = alias;
  }

  public UseClauseTreeImpl(@Nullable InternalSyntaxToken useTypeToken, NamespaceNameTree namespaceName) {
    this.useTypeToken = useTypeToken;
    this.namespaceName = namespaceName;
    this.asToken = null;
    this.alias = null;
  }

  @Nullable
  @Override
  public SyntaxToken useTypeToken() {
    return useTypeToken;
  }

  @Override
  public NamespaceNameTree namespaceName() {
    return namespaceName;
  }

  @Nullable
  @Override
  public SyntaxToken asToken() {
    return asToken;
  }

  @Nullable
  @Override
  public NameIdentifierTree alias() {
    return alias;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(useTypeToken, namespaceName, asToken, alias);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUseClause(this);
  }

}
