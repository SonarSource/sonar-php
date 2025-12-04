/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.statement;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class IfStatementTreeImpl extends PHPTree implements IfStatementTree {

  private final Kind kind;

  private final InternalSyntaxToken ifToken;
  private final ParenthesisedExpressionTree condition;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statements;
  private final List<ElseifClauseTree> elseifClauses;
  private final ElseClauseTree elseClause;
  private final InternalSyntaxToken endifToken;
  private final InternalSyntaxToken eosToken;

  public IfStatementTreeImpl(
    InternalSyntaxToken ifToken, ParenthesisedExpressionTree condition, StatementTree statement,
    List<ElseifClauseTree> elseifClauses, ElseClauseTree elseClause) {
    kind = Kind.IF_STATEMENT;

    this.ifToken = ifToken;
    this.condition = condition;
    this.statements = Collections.singletonList(statement);
    this.elseifClauses = elseifClauses;
    this.elseClause = elseClause;

    this.colonToken = null;
    this.endifToken = null;
    this.eosToken = null;
  }

  public IfStatementTreeImpl(
    InternalSyntaxToken ifToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken,
    List<StatementTree> statements, List<ElseifClauseTree> elseifClauses, ElseClauseTree elseClause,
    InternalSyntaxToken endifToken, InternalSyntaxToken eosToken) {
    kind = Kind.ALTERNATIVE_IF_STATEMENT;

    this.ifToken = ifToken;
    this.condition = condition;
    this.statements = statements;
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
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(ifToken, condition, colonToken),
      statements.iterator(),
      elseifClauses.iterator(),
      IteratorUtils.iteratorOf(elseClause, endifToken, eosToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitIfStatement(this);
  }
}
