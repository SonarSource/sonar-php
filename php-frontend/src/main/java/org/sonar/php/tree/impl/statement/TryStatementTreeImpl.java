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
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class TryStatementTreeImpl extends PHPTree implements TryStatementTree {

  private static final Kind KIND = Kind.TRY_STATEMENT;

  private final InternalSyntaxToken tryToken;
  private final BlockTree block;
  private final List<CatchBlockTree> catchBlocks;
  private final InternalSyntaxToken finallyToken;
  private final BlockTree finallyBlock;

  public TryStatementTreeImpl(InternalSyntaxToken tryToken, BlockTree blockTree, List<CatchBlockTree> catchBlocks, InternalSyntaxToken finallyToken, BlockTree finallyBlock) {
    this.tryToken = tryToken;
    this.block = blockTree;
    this.catchBlocks = catchBlocks;
    this.finallyToken = finallyToken;
    this.finallyBlock = finallyBlock;
  }

  public TryStatementTreeImpl(InternalSyntaxToken tryToken, BlockTree blockTree, List<CatchBlockTree> catchBlocks) {
    this(tryToken, blockTree, catchBlocks, null, null);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
        Iterators.forArray(tryToken, block),
        catchBlocks.iterator(),
        Iterators.forArray(finallyToken, finallyBlock)
    );
  }

  @Override
  public SyntaxToken tryToken() {
    return tryToken;
  }

  @Override
  public BlockTree block() {
    return block;
  }

  @Override
  public List<CatchBlockTree> catchBlocks() {
    return catchBlocks;
  }

  @Nullable
  @Override
  public SyntaxToken finallyToken() {
    return finallyToken;
  }

  @Nullable
  @Override
  public BlockTree finallyBlock() {
    return finallyBlock;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitTryStatement(this);
  }
}
