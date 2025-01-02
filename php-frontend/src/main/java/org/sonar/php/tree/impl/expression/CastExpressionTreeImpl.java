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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.CastExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class CastExpressionTreeImpl extends PHPTree implements CastExpressionTree {

  private static final Kind KIND = Kind.CAST_EXPRESSION;

  private final InternalSyntaxToken openParenthesisToken;
  private final InternalSyntaxToken castType;
  private final InternalSyntaxToken closeParenthesisToken;
  private final ExpressionTree expression;

  public CastExpressionTreeImpl(
    InternalSyntaxToken openParenthesisToken,
    InternalSyntaxToken castType,
    InternalSyntaxToken closeParenthesisToken,
    ExpressionTree expression) {
    this.openParenthesisToken = openParenthesisToken;
    this.castType = castType;
    this.closeParenthesisToken = closeParenthesisToken;
    this.expression = expression;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SyntaxToken castType() {
    return castType;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
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
    return IteratorUtils.iteratorOf(openParenthesisToken, castType, closeParenthesisToken, expression);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCastExpression(this);
  }

}
