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

import java.util.Iterator;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import com.google.common.collect.Iterators;

public class BinaryExpressionTreeImpl extends PHPTree implements BinaryExpressionTree {

  private final Kind kind;
  private final ExpressionTree leftOperand;
  private final InternalSyntaxToken operator;
  private final ExpressionTree rightOperand;

  public BinaryExpressionTreeImpl(Kind kind, ExpressionTree leftOperand, InternalSyntaxToken operator, ExpressionTree rightOperand) {
    this.kind = kind;
    this.leftOperand = leftOperand;
    this.operator = operator;
    this.rightOperand = rightOperand;
  }

  @Override
  public ExpressionTree leftOperand() {
    return leftOperand;
  }

  @Override
  public SyntaxToken operator() {
    return operator;
  }

  @Override
  public ExpressionTree rightOperand() {
    return rightOperand;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(leftOperand, operator, rightOperand);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitBinaryExpression(this);
  }

}
