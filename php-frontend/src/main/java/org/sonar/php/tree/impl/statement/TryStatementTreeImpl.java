/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

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
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(tryToken, block),
      catchBlocks.iterator(),
      IteratorUtils.iteratorOf(finallyToken, finallyBlock));
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
