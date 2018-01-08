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
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.DefaultClauseTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;
import java.util.List;

public class DefaultClauseTreeImpl extends PHPTree implements DefaultClauseTree {

  private static final Kind KIND = Kind.DEFAULT_CLAUSE;

  private final InternalSyntaxToken defaultToken;
  private final InternalSyntaxToken caseSeparatorToken;
  private final List<StatementTree> statements;

  public DefaultClauseTreeImpl(InternalSyntaxToken defaultToken, InternalSyntaxToken caseSeparatorToken, List<StatementTree> statements) {
    this.defaultToken = defaultToken;
    this.caseSeparatorToken = caseSeparatorToken;
    this.statements = statements;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(defaultToken, caseSeparatorToken),
        statements.iterator()
    );
  }

  @Override
  public SyntaxToken caseToken() {
    return defaultToken;
  }

  @Override
  public SyntaxToken caseSeparatorToken() {
    return caseSeparatorToken;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitDefaultClause(this);
  }
}
