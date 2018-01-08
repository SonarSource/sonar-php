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
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class YieldExpressionTreeImpl extends PHPTree implements YieldExpressionTree {

  private static final Kind KIND = Kind.YIELD_EXPRESSION;
  private final InternalSyntaxToken yieldToken;
  private final InternalSyntaxToken fromToken;
  private final ExpressionTree key;
  private final InternalSyntaxToken doubleArrowToken;
  private final ExpressionTree value;

  public YieldExpressionTreeImpl(InternalSyntaxToken yieldToken, ExpressionTree key, InternalSyntaxToken doubleArrowToken, ExpressionTree value) {
    this.yieldToken = yieldToken;
    this.fromToken = null;
    this.key = key;
    this.doubleArrowToken = doubleArrowToken;
    this.value = value;
  }

  public YieldExpressionTreeImpl(InternalSyntaxToken yieldToken, @Nullable ExpressionTree value) {
    this.yieldToken = yieldToken;
    this.fromToken = null;
    this.key = null;
    this.doubleArrowToken = null;
    this.value = value;
  }

  public YieldExpressionTreeImpl(InternalSyntaxToken yieldToken, InternalSyntaxToken fromToken, ExpressionTree value) {
    this.yieldToken = yieldToken;
    this.fromToken = fromToken;
    this.key = null;
    this.doubleArrowToken = null;
    this.value = value;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken yieldToken() {
    return yieldToken;
  }

  @Nullable
  @Override
  public SyntaxToken fromToken() {
    return fromToken;
  }

  @Nullable
  @Override
  public ExpressionTree key() {
    return key;
  }

  @Nullable
  @Override
  public SyntaxToken doubleArrowToken() {
    return doubleArrowToken;
  }

  @Nullable
  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(yieldToken, fromToken, key, doubleArrowToken, value);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitYieldExpression(this);
  }

}
