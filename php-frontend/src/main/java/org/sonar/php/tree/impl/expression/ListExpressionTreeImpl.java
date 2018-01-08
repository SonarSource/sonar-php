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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ListExpressionTreeImpl extends PHPTree implements ListExpressionTree {

  private static final Kind KIND = Kind.LIST_EXPRESSION;
  private final InternalSyntaxToken listToken;
  private final InternalSyntaxToken openParenthesis;
  private final ArrayAssignmentPatternElements elements;
  private final InternalSyntaxToken closeParenthesis;

  public ListExpressionTreeImpl(
    InternalSyntaxToken listToken, InternalSyntaxToken openParenthesis,
    ArrayAssignmentPatternElements elements, InternalSyntaxToken closeParenthesis
  ) {
    this.listToken = listToken;
    this.openParenthesis = openParenthesis;
    this.elements = elements;
    this.closeParenthesis = closeParenthesis;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken listToken() {
    return listToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesis;
  }

  @Override
  public List<Optional<ArrayAssignmentPatternElementTree>> elements() {
    return elements.elements();
  }

  @Override
  public List<SyntaxToken> separators() {
    return elements.separators();
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesis;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(listToken),
      Iterators.singletonIterator(openParenthesis),
      elements.elementsAndSeparators().iterator(),
      Iterators.singletonIterator(closeParenthesis));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitListExpression(this);
  }

}
