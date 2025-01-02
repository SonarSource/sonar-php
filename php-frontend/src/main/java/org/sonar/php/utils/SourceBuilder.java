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
package org.sonar.php.utils;

import java.util.Arrays;
import java.util.List;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import static org.sonar.php.utils.Patterns.LINEBREAK_PATTERN;

public class SourceBuilder extends PHPSubscriptionCheck {

  private final StringBuilder stringBuilder = new StringBuilder();
  private int line = 1;
  private int column = 0;

  public static String build(Tree tree) {
    var writer = new SourceBuilder();
    writer.scanTree(tree);
    return writer.stringBuilder.toString();
  }

  @Override
  public List<Kind> nodesToVisit() {
    return Arrays.asList(Kind.TOKEN, Kind.INLINE_HTML_TOKEN);
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
    String[] lines = LINEBREAK_PATTERN.split(text, -1);
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
