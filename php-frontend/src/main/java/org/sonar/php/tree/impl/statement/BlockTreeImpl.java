/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

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
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openCurlyBraceToken),
      statements.iterator(),
      IteratorUtils.iteratorOf(closeCurlyBraceToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitBlock(this);
  }
}
