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
package org.sonar.php.checks;

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = TrailingCommentCheck.KEY)
public class TrailingCommentCheck extends PHPVisitorCheck {

  public static final String KEY = "S139";
  private static final String MESSAGE = "Move this trailing comment on the previous empty line.";

  public static final String DEFAULT_LEGAL_COMMENT_PATTERN = "^(//|#)\\s*+[^\\s]++$";

  @RuleProperty(
    key = "legalTrailingCommentPattern",
    defaultValue = DEFAULT_LEGAL_COMMENT_PATTERN)
  String legalCommentPattern = DEFAULT_LEGAL_COMMENT_PATTERN;

  private Pattern pattern;
  private int previousTokenLine;

  @Override
  public void init() {
    super.init();
    pattern = Pattern.compile(legalCommentPattern);
  }

  @Override
  public void visitScript(ScriptTree tree) {
    previousTokenLine = -1;
    super.visitScript(tree);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    for (SyntaxTrivia trivia : token.trivias()) {
      if (trivia.line() == previousTokenLine) {
        String comment = trivia.text();
        if ((comment.startsWith("//") || comment.startsWith("#")) && !pattern.matcher(comment).matches()) {
          context().newIssue(this, trivia, MESSAGE);
        }
      }
    }
    previousTokenLine = token.line();
  }
}
