/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ThrowExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ThrowStatementTreeImpl extends PHPTree implements ThrowStatementTree {

  private static final Kind KIND = Kind.THROW_STATEMENT;

  private final ThrowExpressionTree throwExpression;
  private final SyntaxToken eosToken;

  public ThrowStatementTreeImpl(ThrowExpressionTree throwExpression, SyntaxToken eosToken) {
    this.throwExpression = throwExpression;
    this.eosToken = eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(throwExpression, eosToken);
  }

  @Override
  public SyntaxToken throwToken() {
    return throwExpression.throwToken();
  }

  @Override
  public ExpressionTree expression() {
    return throwExpression.expression();
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitThrowStatement(this);
  }
}
