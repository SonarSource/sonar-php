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

import javax.annotation.Nullable;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.collect.Iterators;

public class ArrayAccessTreeImpl extends PHPTree implements ArrayAccessTree {

  private static final Kind KIND = Kind.ARRAY_ACCESS;

  private ExpressionTree object;
  private final InternalSyntaxToken openBracket;
  private final ExpressionTree offset;
  private final InternalSyntaxToken closeBracket;

  public ArrayAccessTreeImpl(InternalSyntaxToken openBracket, ExpressionTree offset, InternalSyntaxToken closeBracket) {
    this.openBracket = openBracket;
    this.offset = offset;
    this.closeBracket = closeBracket;
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
  public SyntaxToken openBracketToken() {
    return openBracket;
  }

  @Nullable
  @Override
  public ExpressionTree offset() {
    return offset;
  }

  @Override
  public SyntaxToken closeBracketToken() {
    return closeBracket;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(object, openBracket, offset, closeBracket);
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitArrayAccess(this);
  }

}
