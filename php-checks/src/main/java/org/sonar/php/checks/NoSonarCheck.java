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

@Rule(key = NoSonarCheck.KEY)
public class NoSonarCheck extends PHPVisitorCheck {

  /**
   * The rule key needs to contain "nosonar" to be allowed to pass the SQ issue filter.
   * @see {@link org.sonar.api.issue.NoSonarFilter#accept}
   */
  public static final String KEY = "NoSonar";

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    int startingLine = trivia.line();
    String[] commentLines = trivia.text().split("(\r)?\n|\r");
    for (String commentLine : commentLines) {
      if (commentLine.contains("NOSONAR")) {
        context().newLineIssue(this, startingLine, "Is //NOSONAR used to exclude false-positive or to hide real quality flaw ?");
      }
      startingLine++;
    }
    super.visitTrivia(trivia);
  }
}
