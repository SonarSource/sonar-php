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
package org.sonar.php.metrics;

import com.google.common.collect.Sets;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.Set;

public class CommentLineVisitor extends PHPVisitorCheck {

  private Set<Integer> comments = Sets.newHashSet();
  private Set<Integer> noSonarLines = Sets.newHashSet();

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
