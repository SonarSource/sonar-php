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
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

public class BreakStatementTreeImpl extends PHPTree implements BreakStatementTree {

  private static final Kind KIND = Kind.BREAK_STATEMENT;

  private final InternalSyntaxToken breakToken;
  private final ExpressionTree argument;
  private final InternalSyntaxToken eosToken;

  public BreakStatementTreeImpl(InternalSyntaxToken breakToken, @Nullable ExpressionTree argument, InternalSyntaxToken eosToken) {
    this.breakToken = breakToken;
    this.argument = argument;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken breakToken() {
    return breakToken;
  }

  @Nullable
  @Override
  public ExpressionTree argument() {
    return argument;
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
    return Iterators.forArray(breakToken, argument, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitBreakStatement(this);
  }
}
