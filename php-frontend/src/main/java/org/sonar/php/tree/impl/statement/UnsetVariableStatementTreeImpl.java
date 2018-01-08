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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class UnsetVariableStatementTreeImpl extends PHPTree implements UnsetVariableStatementTree {

  private static final Kind KIND = Kind.UNSET_VARIABLE_STATEMENT;

  private final InternalSyntaxToken unsetToken;
  private final InternalSyntaxToken openParenthesisToken;
  private final SeparatedListImpl<ExpressionTree> variables;
  private final InternalSyntaxToken closeParenthesisToken;
  private final InternalSyntaxToken eosToken;

  public UnsetVariableStatementTreeImpl(
      InternalSyntaxToken unsetToken, InternalSyntaxToken openParenthesisToken,
      SeparatedListImpl<ExpressionTree> variables,
      InternalSyntaxToken closeParenthesisToken, InternalSyntaxToken eosToken
  ) {
    this.unsetToken = unsetToken;
    this.openParenthesisToken = openParenthesisToken;
    this.variables = variables;
    this.closeParenthesisToken = closeParenthesisToken;
    this.eosToken = eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(unsetToken, openParenthesisToken),
        variables.elementsAndSeparators(),
        Iterators.forArray(closeParenthesisToken, eosToken)
    );
  }

  @Override
  public SyntaxToken unsetToken() {
    return unsetToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedListImpl<ExpressionTree> variables() {
    return variables;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUnsetVariableStatement(this);

  }
}
