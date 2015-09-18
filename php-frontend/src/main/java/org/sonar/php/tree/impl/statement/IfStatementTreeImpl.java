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
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IfStatementTreeImpl extends PHPTree implements IfStatementTree {

  private final Kind KIND;

  private final InternalSyntaxToken ifToken;
  private final ExpressionTree condition;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statement;
  private final List<ElseifClauseTree> elseifClauses;
  private final ElseClauseTree elseClause;
  private final InternalSyntaxToken endifToken;
  private final InternalSyntaxToken eosToken;

  public IfStatementTreeImpl(
      InternalSyntaxToken ifToken, ExpressionTree condition, StatementTree statement,
      List<ElseifClauseTree> elseifClauses, ElseClauseTree elseClause
  ) {
    KIND = Kind.IF_STATEMENT;

    this.ifToken = ifToken;
    this.condition = condition;
    this.statement = Collections.singletonList(statement);
    this.elseifClauses = elseifClauses;
    this.elseClause = elseClause;

    this.colonToken = null;
    this.endifToken = null;
    this.eosToken = null;
  }

  public IfStatementTreeImpl(
      InternalSyntaxToken ifToken, ExpressionTree condition, InternalSyntaxToken colonToken,
      List<StatementTree> statements, List<ElseifClauseTree> elseifClauses, ElseClauseTree elseClause,
      InternalSyntaxToken endifToken, InternalSyntaxToken eosToken
  ) {
    KIND = Kind.ALTRNATIVE_IF_STATEMENT;

    this.ifToken = ifToken;
    this.condition = condition;
    this.statement = statements;
    this.elseifClauses = elseifClauses;
    this.elseClause = elseClause;

    this.colonToken = colonToken;
    this.endifToken = endifToken;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken ifToken() {
    return ifToken;
  }

  @Override
  public ExpressionTree condition() {
    return condition;
  }

  @Nullable
  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public List<StatementTree> statement() {
    return statement;
  }

  @Override
  public List<ElseifClauseTree> elseifClauses() {
    return elseifClauses;
  }

  @Nullable
  @Override
  public ElseClauseTree elseClause() {
    return elseClause;
  }

  @Nullable
  @Override
  public SyntaxToken endifToken() {
    return endifToken;
  }

  @Nullable
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
    return Iterators.concat(
        Iterators.forArray(ifToken, condition, colonToken),
        statement.iterator(),
        elseifClauses.iterator(),
        Iterators.forArray(elseClause, endifToken, eosToken)
    );
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitIfStatement(this);
  }
}
