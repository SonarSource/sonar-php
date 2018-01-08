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
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class DoWhileStatementTreeImpl extends PHPTree implements DoWhileStatementTree {

  private static final Kind KIND = Kind.DO_WHILE_STATEMENT;

  private final InternalSyntaxToken doToken;
  private final StatementTree statement;
  private final InternalSyntaxToken whileToken;
  private final ParenthesisedExpressionTree condition;
  private final InternalSyntaxToken eosToken;

  public DoWhileStatementTreeImpl(
      InternalSyntaxToken doToken, StatementTree statement,
      InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition,
      InternalSyntaxToken eosToken
  ) {
    this.doToken = doToken;
    this.statement = statement;
    this.whileToken = whileToken;
    this.condition = condition;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken doToken() {
    return doToken;
  }

  @Override
  public StatementTree statement() {
    return statement;
  }

  @Override
  public SyntaxToken whileToken() {
    return whileToken;
  }

  @Override
  public ParenthesisedExpressionTree condition() {
    return condition;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(doToken, statement, whileToken, condition, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitDoWhileStatement(this);
  }
}
