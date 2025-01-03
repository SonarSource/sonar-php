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
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ConditionalExpressionTreeImpl extends PHPTree implements ConditionalExpressionTree {

  private static final Kind KIND = Kind.CONDITIONAL_EXPRESSION;

  private ExpressionTree condition;
  private final SyntaxToken queryToken;
  @Nullable
  private final ExpressionTree trueExpression;
  private final SyntaxToken colonToken;
  private final ExpressionTree falseExpression;

  public ConditionalExpressionTreeImpl(InternalSyntaxToken queryToken, @Nullable ExpressionTree trueExpression, InternalSyntaxToken colonToken, ExpressionTree falseExpression) {
    this.queryToken = queryToken;
    this.trueExpression = trueExpression;
    this.colonToken = colonToken;
    this.falseExpression = falseExpression;
  }

  public ConditionalExpressionTreeImpl complete(ExpressionTree condition) {
    this.condition = condition;

    return this;
  }

  @Override
  public ExpressionTree condition() {
    return condition;
  }

  @Override
  public SyntaxToken queryToken() {
    return queryToken;
  }

  @Nullable
  @Override
  public ExpressionTree trueExpression() {
    return trueExpression;
  }

  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public ExpressionTree falseExpression() {
    return falseExpression;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(condition, queryToken, trueExpression, colonToken, falseExpression);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitConditionalExpression(this);
  }

}
