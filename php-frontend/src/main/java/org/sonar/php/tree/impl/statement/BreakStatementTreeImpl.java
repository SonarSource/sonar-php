/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class BreakStatementTreeImpl extends PHPTree implements BreakStatementTree {

  private static final Kind KIND = Kind.BREAK_STATEMENT;

  private final InternalSyntaxToken breakToken;
  private final ExpressionTree argument;
  private final InternalSyntaxToken eosToken;

  public BreakStatementTreeImpl(InternalSyntaxToken breakToken, @Nullable ExpressionTree argument, InternalSyntaxToken eosToken) {
    this.breakToken = breakToken;
    this.argument = argument;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken breakToken() {
    return breakToken;
  }

  @Nullable
  @Override
  public ExpressionTree argument() {
    return argument;
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
    return IteratorUtils.iteratorOf(breakToken, argument, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitBreakStatement(this);
  }
}
