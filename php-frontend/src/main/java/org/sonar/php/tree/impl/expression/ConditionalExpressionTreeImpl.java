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
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

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
    return Iterators.forArray(condition, queryToken, trueExpression, colonToken, falseExpression);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitConditionalExpression(this);
  }

}
