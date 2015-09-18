/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ElseClauseTreeImpl extends PHPTree implements ElseClauseTree {

  private final Kind KIND;

  private final InternalSyntaxToken elseToken;
  private final InternalSyntaxToken colonToken;
  private final List<StatementTree> statement;

  public ElseClauseTreeImpl(InternalSyntaxToken elseToken, StatementTree statement) {
    this.KIND = Kind.ELSE_CLAUSE;
    this.elseToken = elseToken;
    this.statement = Collections.singletonList(statement);
    this.colonToken = null;
  }

  public ElseClauseTreeImpl(InternalSyntaxToken elseToken, InternalSyntaxToken colonToken, List<StatementTree> statements) {
    this.KIND = Kind.ALTERNATIVE_ELSE_CLAUSE;
    this.elseToken = elseToken;
    this.statement = statements;
    this.colonToken = colonToken;
  }

  @Override
  public SyntaxToken elseToken() {
    return elseToken;
  }

  @Nullable
  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public List<StatementTree> statement() {
    return statement;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(elseToken, colonToken),
        statement.iterator()
    );
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitElseClause(this);
  }
}
