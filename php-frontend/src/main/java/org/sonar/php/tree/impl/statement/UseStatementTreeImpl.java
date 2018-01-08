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

import com.google.common.collect.Iterators;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class UseStatementTreeImpl extends PHPTree implements UseStatementTree {

  private final Kind kind;
  private final InternalSyntaxToken useToken;
  private final InternalSyntaxToken useTypeToken;
  private final NamespaceNameTree prefix;
  private final InternalSyntaxToken nsSeparatorToken;
  private final InternalSyntaxToken openCurlyBraceToken;
  private final SeparatedListImpl<UseClauseTree> clauses;
  private final InternalSyntaxToken closeCurlyBraceToken;
  private final InternalSyntaxToken eosToken;

  private UseStatementTreeImpl(
    Tree.Kind kind,
    InternalSyntaxToken useToken,
    @Nullable InternalSyntaxToken useTypeToken,
    @Nullable NamespaceNameTree prefix,
    @Nullable InternalSyntaxToken nsSeparatorToken,
    @Nullable InternalSyntaxToken openCurlyBraceToken,
    SeparatedListImpl<UseClauseTree> clauses,
    @Nullable InternalSyntaxToken closeCurlyBraceToken,
    InternalSyntaxToken eosToken
  ) {
    this.useToken = useToken;
    this.useTypeToken = useTypeToken;
    this.prefix = prefix;
    this.nsSeparatorToken = nsSeparatorToken;
    this.openCurlyBraceToken = openCurlyBraceToken;
    this.clauses = clauses;
    this.closeCurlyBraceToken = closeCurlyBraceToken;
    this.eosToken = eosToken;

    this.kind = kind;
  }

  public static UseStatementTreeImpl createUseStatement(
    InternalSyntaxToken useToken,
    @Nullable InternalSyntaxToken useTypeToken,
    SeparatedListImpl<UseClauseTree> clauses,
    InternalSyntaxToken eosToken
  ) {
    return new UseStatementTreeImpl(Kind.USE_STATEMENT, useToken, useTypeToken, null, null, null, clauses, null, eosToken);
  }

  public static UseStatementTreeImpl createGroupUseStatement(
    InternalSyntaxToken useToken,
    @Nullable InternalSyntaxToken useTypeToken,
    NamespaceNameTree prefix,
    InternalSyntaxToken nsSeparatorToken,
    InternalSyntaxToken openCurlyBraceToken,
    SeparatedListImpl<UseClauseTree> clauses,
    InternalSyntaxToken closeCurlyBraceToken,
    InternalSyntaxToken eosToken
  ) {
    return new UseStatementTreeImpl(Kind.GROUP_USE_STATEMENT, useToken, useTypeToken, prefix, nsSeparatorToken, openCurlyBraceToken, clauses, closeCurlyBraceToken, eosToken);
  }

  @Override
  public SyntaxToken useToken() {
    return useToken;
  }

  @Override
  public SyntaxToken useTypeToken() {
    return useTypeToken;
  }

  @Nullable
  @Override
  public NamespaceNameTree prefix() {
    return prefix;
  }

  @Nullable
  @Override
  public SyntaxToken nsSeparatorToken() {
    return nsSeparatorToken;
  }

  @Nullable
  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Override
  public SeparatedListImpl<UseClauseTree> clauses() {
    return clauses;
  }

  @Nullable
  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.forArray(useToken, useTypeToken, prefix, nsSeparatorToken, openCurlyBraceToken),
      clauses.elementsAndSeparators(),
      Iterators.forArray(closeCurlyBraceToken, eosToken)
      );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUseStatement(this);
  }
}

