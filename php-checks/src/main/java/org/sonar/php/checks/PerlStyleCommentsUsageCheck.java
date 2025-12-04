/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2046")
public class PerlStyleCommentsUsageCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Use \"//\" instead of \"#\" to start this comment";

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    super.visitTrivia(trivia);

    String text = trivia.text();
    if (text.charAt(0) == '#' && !isShebangLine(text)) {
      context().newIssue(this, trivia, MESSAGE);
    }
  }

  private static boolean isShebangLine(String triviaText) {
    return triviaText.startsWith("#!");
  }

}
