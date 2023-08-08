/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MatchClauseTree;
import org.sonar.plugins.php.api.tree.expression.MatchExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class MatchExpressionTreeImpl extends PHPTree implements MatchExpressionTree {

  private final Kind kind;

  private final SyntaxToken matchToken;
  private final SyntaxToken openParenthesis;
  private final ExpressionTree expression;
  private final SyntaxToken closeParenthesis;
  private final SyntaxToken openCurlyBraceToken;
  private final SeparatedList<MatchClauseTree> cases;
  private final SyntaxToken closeCurlyBraceToken;

  public MatchExpressionTreeImpl(SyntaxToken matchToken, SyntaxToken openParenthesis, ExpressionTree expression, SyntaxToken closeParenthesis, SyntaxToken openCurlyBraceToken,
    SeparatedList<MatchClauseTree> cases,
    SyntaxToken closeCurlyBraceToken) {

    this.kind = Kind.MATCH_EXPRESSION;

    this.matchToken = matchToken;
    this.openParenthesis = openParenthesis;
    this.expression = expression;
    this.closeParenthesis = closeParenthesis;
    this.openCurlyBraceToken = openCurlyBraceToken;
    this.cases = cases;
    this.closeCurlyBraceToken = closeCurlyBraceToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitMatchExpression(this);
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public SyntaxToken matchToken() {
    return matchToken;
  }

  @Override
  public SyntaxToken openParenthesis() {
    return openParenthesis;
  }

  @Override
  public SyntaxToken closeParenthesis() {
    return closeParenthesis;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Override
  public SeparatedList<MatchClauseTree> cases() {
    return cases;
  }

  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(matchToken, openParenthesis, expression, closeParenthesis, openCurlyBraceToken),
      cases.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeCurlyBraceToken));
  }
}
