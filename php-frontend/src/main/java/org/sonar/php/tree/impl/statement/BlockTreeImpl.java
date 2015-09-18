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
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import java.util.Iterator;
import java.util.List;

public class BlockTreeImpl extends PHPTree implements BlockTree {

  private static final Kind KIND = Kind.BLOCK;

  private final InternalSyntaxToken openCurlyBraceToken;
  private final List<StatementTree> statements;
  private final InternalSyntaxToken closeCurlyBraceToken;

  public BlockTreeImpl(InternalSyntaxToken lbrace, List<StatementTree> statements, InternalSyntaxToken rbrace) {
    this.openCurlyBraceToken = lbrace;
    this.statements = statements;
    this.closeCurlyBraceToken = rbrace;
  }

  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.singletonIterator(openCurlyBraceToken),
        statements.iterator(),
        Iterators.singletonIterator(closeCurlyBraceToken));
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitBlock(this);
  }
}
