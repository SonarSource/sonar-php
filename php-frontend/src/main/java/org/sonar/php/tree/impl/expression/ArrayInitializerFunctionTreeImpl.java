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
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerFunctionTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class ArrayInitializerFunctionTreeImpl extends PHPTree implements ArrayInitializerFunctionTree {

  private static final Kind KIND = Kind.ARRAY_INITIALIZER_FUNCTION;
  private final InternalSyntaxToken arrayToken;
  private final InternalSyntaxToken openParenthesis;
  private final SeparatedListImpl<ArrayPairTree> arrayPairs;
  private final InternalSyntaxToken closeParenthesis;

  public ArrayInitializerFunctionTreeImpl(
    InternalSyntaxToken arrayToken,
    InternalSyntaxToken openParenthesis,
    SeparatedListImpl<ArrayPairTree> arrayPairs,
    InternalSyntaxToken closeParenthesis
  ) {
    this.arrayToken = arrayToken;
    this.openParenthesis = openParenthesis;
    this.arrayPairs = arrayPairs;
    this.closeParenthesis = closeParenthesis;
  }

  @Override
  public SyntaxToken arrayToken() {
    return arrayToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesis;
  }

  @Override
  public SeparatedListImpl<ArrayPairTree> arrayPairs() {
    return arrayPairs;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesis;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(arrayToken),
      Iterators.singletonIterator(openParenthesis),
      arrayPairs.elementsAndSeparators(),
      Iterators.singletonIterator(closeParenthesis));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayInitializerFunction(this);
  }

}
