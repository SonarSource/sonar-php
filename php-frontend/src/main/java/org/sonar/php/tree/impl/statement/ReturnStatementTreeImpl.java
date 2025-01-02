/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ReturnStatementTreeImpl extends PHPTree implements ReturnStatementTree {

  private static final Kind KIND = Kind.RETURN_STATEMENT;

  private final InternalSyntaxToken returnToken;
  private final ExpressionTree expression;
  private final InternalSyntaxToken eosToken;

  public ReturnStatementTreeImpl(InternalSyntaxToken returnToken, @Nullable ExpressionTree expression, InternalSyntaxToken eosToken) {
    this.returnToken = returnToken;
    this.expression = expression;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken returnToken() {
    return returnToken;
  }

  @Nullable
  @Override
  public ExpressionTree expression() {
    return expression;
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
    return IteratorUtils.iteratorOf(returnToken, expression, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitReturnStatement(this);
  }
}
