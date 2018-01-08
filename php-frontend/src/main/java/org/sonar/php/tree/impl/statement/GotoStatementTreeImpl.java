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
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class GotoStatementTreeImpl extends PHPTree implements GotoStatementTree {

  private static final Kind KIND = Kind.GOTO_STATEMENT;

  private final InternalSyntaxToken gotoToken;
  private final NameIdentifierTree identifier;
  private final InternalSyntaxToken eosToken;

  public GotoStatementTreeImpl(InternalSyntaxToken gotoToken, NameIdentifierTree identifier, InternalSyntaxToken eosToken) {
    this.gotoToken = gotoToken;
    this.identifier = identifier;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken gotoToken() {
    return gotoToken;
  }

  @Override
  public NameIdentifierTree identifier() {
    return identifier;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitGotoStatement(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(gotoToken, identifier, eosToken);
  }
}
