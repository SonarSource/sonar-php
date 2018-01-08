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
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ExpressionListStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ExpressionListStatementTreeImpl extends PHPTree implements ExpressionListStatementTree {

  private static final Kind KIND = Kind.EXPRESSION_LIST_STATEMENT;

  private final SeparatedList<ExpressionTree> expressions;
  private final InternalSyntaxToken eosToken;

  public ExpressionListStatementTreeImpl(SeparatedList<ExpressionTree> expressions, InternalSyntaxToken eosToken) {
    this.expressions = expressions;
    this.eosToken = eosToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(expressions.elementsAndSeparators(), Iterators.singletonIterator(eosToken));
  }

  @Override
  public SeparatedList<ExpressionTree> expressions() {
    return expressions;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExpressionListStatement(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }
}
