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
package org.sonar.php.checks.utils;

import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public abstract class AbstractCommentContainsPatternCheck extends PHPVisitorCheck {

  protected abstract String pattern();

  protected abstract String message();

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    String comment = trivia.text();

    if (StringUtils.containsIgnoreCase(comment, pattern())) {
      String[] lines = comment.split("\r\n?|\n");

      for (int i = 0; i < lines.length; i++) {
        if (StringUtils.containsIgnoreCase(lines[i], pattern()) && !isLetterAround(lines[i])) {
          createIssue(trivia.line() + i);
        }
      }
    }
  }

  private boolean isLetterAround(String line) {
    int start = StringUtils.indexOfIgnoreCase(line, pattern());
    int end = start + pattern().length();

    boolean pre = start > 0 && Character.isLetter(line.charAt(start - 1));
    boolean post = end < line.length() - 1 && Character.isLetter(line.charAt(end));

    return pre || post;
  }

  private void createIssue(int line) {
    context().newLineIssue(this, line, message());
  }

}
