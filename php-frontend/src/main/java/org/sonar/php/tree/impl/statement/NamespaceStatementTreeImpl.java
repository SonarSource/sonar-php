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
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class NamespaceStatementTreeImpl extends PHPTree implements NamespaceStatementTree {

  private static final Kind KIND = Kind.NAMESPACE_STATEMENT;

  private final InternalSyntaxToken namespaceToken;
  private final NamespaceNameTree namespaceName;
  private final InternalSyntaxToken openCurlyBrace;
  private final List<StatementTree> statements;
  private final InternalSyntaxToken closeCurlyBrace;
  private final InternalSyntaxToken eosToken;

  public NamespaceStatementTreeImpl(InternalSyntaxToken namespaceToken, NamespaceNameTree namespaceName, InternalSyntaxToken eosToken) {
    this.namespaceToken = namespaceToken;
    this.namespaceName = namespaceName;
    this.eosToken = eosToken;

    this.openCurlyBrace = null;
    this.statements = ImmutableList.of();
    this.closeCurlyBrace = null;
  }

  public NamespaceStatementTreeImpl(
      InternalSyntaxToken namespaceToken, @Nullable NamespaceNameTree namespaceName,
      InternalSyntaxToken openCurlyBrace, List<StatementTree> statements, InternalSyntaxToken closeCurlyBrace
  ) {
    this.namespaceToken = namespaceToken;
    this.namespaceName = namespaceName;
    this.openCurlyBrace = openCurlyBrace;
    this.statements = statements;
    this.closeCurlyBrace = closeCurlyBrace;

    this.eosToken = null;
  }

  @Override
  public InternalSyntaxToken namespaceToken() {
    return namespaceToken;
  }

  @Nullable
  @Override
  public NamespaceNameTree namespaceName() {
    return namespaceName;
  }

  @Nullable
  @Override
  public InternalSyntaxToken openCurlyBrace() {
    return openCurlyBrace;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Nullable
  @Override
  public InternalSyntaxToken closeCurlyBrace() {
    return closeCurlyBrace;
  }

  @Nullable
  @Override
  public InternalSyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(namespaceToken, namespaceName, openCurlyBrace),
        statements.iterator(),
        Iterators.forArray(closeCurlyBrace, eosToken)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitNamespaceStatement(this);
  }
}
