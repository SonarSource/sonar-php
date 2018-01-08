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

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import com.google.common.collect.Iterators;

public class ArrayAccessTreeImpl extends PHPTree implements ArrayAccessTree {

  private static final Kind KIND = Kind.ARRAY_ACCESS;

  private ExpressionTree object;
  private final InternalSyntaxToken openBraceToken;
  @Nullable
  private final ExpressionTree offset;
  private final InternalSyntaxToken closeBraceToken;

  public ArrayAccessTreeImpl(InternalSyntaxToken openBraceToken, ExpressionTree offset, InternalSyntaxToken closeBraceToken) {
    this.openBraceToken = openBraceToken;
    this.offset = offset;
    this.closeBraceToken = closeBraceToken;
  }

  public ArrayAccessTreeImpl(InternalSyntaxToken openBraceToken, InternalSyntaxToken closeBraceToken) {
    this.openBraceToken = openBraceToken;
    this.offset = null;
    this.closeBraceToken = closeBraceToken;
  }

  public ArrayAccessTree complete(ExpressionTree object) {
    this.object = object;

    return this;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public ExpressionTree object() {
    return object;
  }

  @Override
  public SyntaxToken openBraceToken() {
    return openBraceToken;
  }

  @Nullable
  @Override
  public ExpressionTree offset() {
    return offset;
  }

  @Override
  public SyntaxToken closeBraceToken() {
    return closeBraceToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(object, openBraceToken, offset, closeBraceToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayAccess(this);
  }

}
