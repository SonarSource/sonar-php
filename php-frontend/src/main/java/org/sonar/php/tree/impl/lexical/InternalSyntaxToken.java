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
package org.sonar.php.tree.impl.lexical;

import com.google.common.collect.Iterators;
import com.sonar.sslr.api.TokenType;
import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class InternalSyntaxToken extends PHPTree implements SyntaxToken {

  private final Kind kind;

  private final List<SyntaxTrivia> trivias;
  private final int startIndex;
  private final int line;
  private final int column;
  private final String value;
  private final boolean isEOF;
  private int endLine;
  private int endColumn;

  public InternalSyntaxToken(int line, int column, String value, List<SyntaxTrivia> trivias, int startIndex, boolean isEOF) {
    this.value = value;
    this.line = line;
    this.column = column;
    this.trivias = trivias;
    this.startIndex = startIndex;
    this.isEOF = isEOF;
    this.kind = isInlineHTML(value) ? Kind.INLINE_HTML_TOKEN : Kind.TOKEN;
    calculateEndOffsets();
  }

  private void calculateEndOffsets() {
    String[] lines = value.split("\r\n|\n|\r", -1);
    endColumn = column + value.length();
    endLine = line + lines.length - 1;

    if (endLine != line) {
      endColumn = lines[lines.length - 1].length();
    }
  }

  private static boolean isInlineHTML(String value) {
    return value.startsWith("?>") || value.startsWith("%>");
  }

  public int toIndex() {
    return startIndex + value.length();
  }

  @Override
  public String text() {
    return value;
  }

  @Override
  public List<SyntaxTrivia> trivias() {
    return trivias;
  }

  @Override
  public int line() {
    return line;
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

  public int startIndex() {
    return startIndex;
  }

  public boolean isEOF() {
    return isEOF;
  }

  public boolean is(TokenType type) {
    return this.text().equals(type.getValue());
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.<Tree>concat(trivias().iterator());
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitToken(this);
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
