/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.tree.impl.lexical;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import static org.sonar.php.utils.Patterns.LINEBREAK_PATTERN;

public class InternalSyntaxTrivia extends PHPTree implements SyntaxTrivia {

  private final String comment;
  private final int column;
  private int startLine;
  private int endLine;
  private int endColumn;

  public InternalSyntaxTrivia(String comment, int startLine, int column) {
    this.comment = comment;
    this.startLine = startLine;
    this.column = column;
    calculateEndOffsets();
  }

  private void calculateEndOffsets() {
    String[] lines = LINEBREAK_PATTERN.split(comment, -1);
    endColumn = column + comment.length();
    endLine = startLine + lines.length - 1;

    if (endLine != startLine) {
      endColumn = lines[lines.length - 1].length();
    }
  }

  @Override
  public String text() {
    return comment;
  }

  @Override
  public List<SyntaxTrivia> trivias() {
    return Collections.emptyList();
  }

  @Override
  public int line() {
    return startLine;
  }

  @Override
  public int column() {
    return column;
  }

  @Override
  public int endLine() {
    return endLine;
  }

  @Override
  public int endColumn() {
    return endColumn;
  }

  @Override
  public Kind getKind() {
    return Kind.TRIVIA;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    throw new UnsupportedOperationException();
  }

  public static SyntaxTrivia create(String comment, int startLine, int column) {
    return new InternalSyntaxTrivia(comment, startLine, column);
  }

  @Override
  public int getLine() {
    return startLine;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitTrivia(this);
  }

  @Override
  public SyntaxToken getFirstToken() {
    return this;
  }

  @Override
  public SyntaxToken getLastToken() {
    return this;
  }
}
