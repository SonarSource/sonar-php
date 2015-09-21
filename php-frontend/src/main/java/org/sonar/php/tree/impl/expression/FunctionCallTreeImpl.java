/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.base.Functions;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;

public class FunctionCallTreeImpl extends PHPTree implements FunctionCallTree {

  private static final Kind KIND = Kind.FUNCTION_CALL;
  private ExpressionTree callee;
  private final InternalSyntaxToken openParenthesisToken;
  private final SeparatedList arguments;
  private final InternalSyntaxToken closeParenthesisToken;

  public FunctionCallTreeImpl(InternalSyntaxToken openParenthesisToken, SeparatedList arguments, InternalSyntaxToken closeParenthesisToken) {
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = arguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  public FunctionCallTreeImpl complete(ExpressionTree callee) {
    this.callee = callee;

    return this;
  }

  @Override
  public ExpressionTree callee() {
    return callee;
  }

  @Nullable
  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedList<ExpressionTree> arguments() {
    return arguments;
  }

  @Nullable
  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(callee),
      Iterators.singletonIterator(openParenthesisToken),
      arguments.elementsAndSeparators(Functions.<ExpressionTree>identity()),
      Iterators.singletonIterator(closeParenthesisToken));
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitFunctionCall(this);
  }

}
