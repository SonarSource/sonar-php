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
import org.sonar.plugins.php.api.tree.expression.MatchConditionClauseTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class MatchConditionClauseTreeImpl extends PHPTree implements MatchConditionClauseTree {

  private static final Kind KIND = Kind.MATCH_CONDITION_CLAUSE;

  private final SeparatedList<ExpressionTree> conditions;
  private final SyntaxToken doubleArrowToken;
  private final ExpressionTree expression;

  public MatchConditionClauseTreeImpl(SeparatedList<ExpressionTree> conditions, SyntaxToken doubleArrowToken, ExpressionTree expression) {
    this.conditions = conditions;
    this.doubleArrowToken = doubleArrowToken;
    this.expression = expression;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitMatchConditionClause(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SeparatedList<ExpressionTree> conditions() {
    return conditions;
  }

  @Override
  public SyntaxToken doubleArrowToken() {
    return doubleArrowToken;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(conditions.elementsAndSeparators(), IteratorUtils.iteratorOf(doubleArrowToken, expression));
  }

}
