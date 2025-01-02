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
package org.sonar.php.metrics;

import java.util.HashSet;
import java.util.Set;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class CommentLineVisitor extends PHPVisitorCheck {

  private Set<Integer> comments = new HashSet<>();
  private Set<Integer> noSonarLines = new HashSet<>();

  public CommentLineVisitor(CompilationUnitTree tree) {
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    for (SyntaxTrivia trivia : token.trivias()) {

      String[] commentLines = getContents(trivia.text())
        .split("(\r)?\n|\r", -1);
      int line = trivia.line();
      for (String commentLine : commentLines) {
        if (commentLine.contains("NOSONAR")) {
          noSonarLines.add(line);
        } else if (!isBlank(commentLine)) {
          comments.add(line);
        }
        line++;
      }

    }

    super.visitToken(token);
  }

  private static boolean isBlank(CharSequence line) {
    for (int i = 0; i < line.length(); i++) {
      if (Character.isLetterOrDigit(line.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  private static String getContents(String comment) {
    if (comment.startsWith("//")) {
      return comment.substring(2);
    } else if (comment.startsWith("#")) {
      return comment.substring(1);
    } else {
      return comment.substring(2, comment.length() - 2);
    }
  }

  public Set<Integer> noSonarLines() {
    return noSonarLines;
  }

  public Set<Integer> commentLines() {
    return comments;
  }

  public int commentLineNumber() {
    return comments.size();
  }
}
