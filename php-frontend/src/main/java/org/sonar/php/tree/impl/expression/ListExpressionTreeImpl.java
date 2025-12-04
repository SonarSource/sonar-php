/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import java.util.List;
import java.util.Optional;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
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
    ArrayAssignmentPatternElements elements, InternalSyntaxToken closeParenthesis) {
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
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(listToken),
      IteratorUtils.iteratorOf(openParenthesis),
      elements.elementsAndSeparators().iterator(),
      IteratorUtils.iteratorOf(closeParenthesis));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitListExpression(this);
  }

}
