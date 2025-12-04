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

import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class CaseClauseTreeImpl extends PHPTree implements CaseClauseTree {

  private static final Kind KIND = Kind.CASE_CLAUSE;

  private final InternalSyntaxToken caseToken;
  private final ExpressionTree expression;
  private final InternalSyntaxToken caseSeparatorToken;
  private final List<StatementTree> statements;

  public CaseClauseTreeImpl(InternalSyntaxToken caseToken, ExpressionTree expression, InternalSyntaxToken caseSeparatorToken, List<StatementTree> statements) {
    this.caseToken = caseToken;
    this.expression = expression;
    this.caseSeparatorToken = caseSeparatorToken;
    this.statements = statements;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(caseToken, expression, caseSeparatorToken),
      statements.iterator());
  }

  @Override
  public SyntaxToken caseToken() {
    return caseToken;
  }

  @Override
  public SyntaxToken caseSeparatorToken() {
    return caseSeparatorToken;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCaseClause(this);
  }
}
