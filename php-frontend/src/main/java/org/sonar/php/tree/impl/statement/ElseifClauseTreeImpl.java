/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ElseifClauseTreeImpl extends PHPTree implements ElseifClauseTree {

  private final Kind kind;

  private final InternalSyntaxToken elseifToken;
  private final ParenthesisedExpressionTree condition;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statements;

  public ElseifClauseTreeImpl(InternalSyntaxToken elseifToken, ParenthesisedExpressionTree condition, StatementTree statement) {
    this.kind = Kind.ELSEIF_CLAUSE;

    this.elseifToken = elseifToken;
    this.condition = condition;
    this.statements = Collections.singletonList(statement);

    this.colonToken = null;
  }

  public ElseifClauseTreeImpl(InternalSyntaxToken elseifToken, ParenthesisedExpressionTree condition, InternalSyntaxToken colonToken, List<StatementTree> statements) {
    this.kind = Kind.ALTERNATIVE_ELSEIF_CLAUSE;

    this.elseifToken = elseifToken;
    this.condition = condition;
    this.statements = statements;

    this.colonToken = colonToken;
  }

  @Override
  public SyntaxToken elseifToken() {
    return elseifToken;
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
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(elseifToken, condition, colonToken),
      statements.iterator());
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitElseifClause(this);
  }
}
