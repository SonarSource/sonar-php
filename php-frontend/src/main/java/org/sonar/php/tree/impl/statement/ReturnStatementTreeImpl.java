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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class ReturnStatementTreeImpl extends PHPTree implements ReturnStatementTree {

  private static final Kind KIND = Kind.RETURN_STATEMENT;

  private final InternalSyntaxToken returnToken;
  private final ExpressionTree expression;
  private final InternalSyntaxToken eosToken;

  public ReturnStatementTreeImpl(InternalSyntaxToken returnToken, @Nullable ExpressionTree expression, InternalSyntaxToken eosToken) {
    this.returnToken = returnToken;
    this.expression = expression;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken returnToken() {
    return returnToken;
  }

  @Nullable
  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Nullable
  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(returnToken, expression, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitReturnStatement(this);
  }
}
