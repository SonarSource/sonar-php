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
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WhileStatementTreeImpl extends PHPTree implements WhileStatementTree {

  private final Kind kind;

  private final InternalSyntaxToken whileToken;
  private final ParenthesisedExpressionTree condition;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statements;
  private final InternalSyntaxToken endwhileToken;
  private final InternalSyntaxToken eosToken;

  public WhileStatementTreeImpl(InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, StatementTree statement) {
    this.kind = Kind.WHILE_STATEMENT;

    this.whileToken = whileToken;
    this.condition = condition;
    this.statements = Collections.singletonList(statement);

    this.colonToken = null;
    this.endwhileToken = null;
    this.eosToken = null;
  }

  public WhileStatementTreeImpl(
      InternalSyntaxToken whileToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
      List<StatementTree> statements, InternalSyntaxToken endwhileToken, InternalSyntaxToken eosToken
  ) {
    this.kind = Kind.ALTERNATIVE_WHILE_STATEMENT;

    this.whileToken = whileToken;
    this.condition = condition;
    this.statements = statements;

    this.colonToken = colonToken;
    this.endwhileToken = endwhileToken;
    this.eosToken = eosToken;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(whileToken, condition, colonToken),
        statements.iterator(),
        Iterators.forArray(endwhileToken, eosToken)
    );
  }

  @Override
  public SyntaxToken whileToken() {
    return whileToken;
  }

  @Override
  public ParenthesisedExpressionTree condition() {
    return condition;
  }

  @Nullable
  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Nullable
  @Override
  public SyntaxToken endWhileToken() {
    return endwhileToken;
  }

  @Nullable
  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitWhileStatement(this);
  }
}
