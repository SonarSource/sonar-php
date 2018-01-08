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
package org.sonar.php.utils;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

public class SourceBuilder extends PHPSubscriptionCheck {

  private final StringBuilder stringBuilder = new StringBuilder();
  private int line = 1;
  private int column = 0;

  public static String build(Tree tree) {
    SourceBuilder writer = new SourceBuilder();
    writer.scanTree(tree);
    return writer.stringBuilder.toString();
  }

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.TOKEN, Kind.INLINE_HTML_TOKEN);
  }

  @Override
  public void visitNode(Tree tree) {
    SyntaxToken token = (SyntaxToken) tree;
    for (SyntaxTrivia trivia : token.trivias()) {
      appendToken(trivia);
    }
    appendToken(token);
  }

  private void appendToken(SyntaxToken token) {
    insertMissingSpaceBefore(token.line(), token.column());
    String text = token.text();
    stringBuilder.append(text);
    String[] lines = text.split("\r\n|\n|\r", -1);
    if (lines.length > 1) {
      line += lines.length - 1;
      column = lines[lines.length - 1].length();
    } else {
      column += text.length();
    }
  }

  private void insertMissingSpaceBefore(int tokenLine, int tokenColumn) {
    int linesToInsert = tokenLine - line;
    if (linesToInsert < 0) {
      throw new IllegalStateException("Illegal token line for " + tokenLine);
    } else if (linesToInsert > 0) {
      for (int i = 0; i < linesToInsert; i++) {
        stringBuilder.append("\n");
        line++;
      }
      column = 0;
    }
    int spacesToInsert = tokenColumn - column;
    for (int i = 0; i < spacesToInsert; i++) {
      stringBuilder.append(' ');
      column++;
    }
  }

}
