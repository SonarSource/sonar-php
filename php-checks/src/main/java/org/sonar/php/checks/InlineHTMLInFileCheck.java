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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1997")
public class InlineHTMLInFileCheck extends PHPVisitorCheck {

  public static final String KEY = "S1997";
  private static final String MESSAGE = "Remove the inline HTML in this file.";

  private boolean fileHasIssue;

  @Override
  public void visitToken(SyntaxToken token) {
    if (token.is(Kind.INLINE_HTML_TOKEN) && !CheckUtils.isClosingTag(token)) {
      fileHasIssue = true;
    }
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    if (!isExcludedFile()) {
      fileHasIssue = false;
      super.visitCompilationUnit(tree);
      if (fileHasIssue) {
        context().newFileIssue(this, MESSAGE);
      }
    }
  }

  private boolean isExcludedFile() {
    return context().getPhpFile().filename().endsWith(".phtml");
  }

}
