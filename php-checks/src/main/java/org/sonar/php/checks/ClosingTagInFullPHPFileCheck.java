/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ClosingTagInFullPHPFileCheck.KEY)
public class ClosingTagInFullPHPFileCheck extends PHPVisitorCheck {

  public static final String KEY = "S1780";
  private static final String MESSAGE = "Remove this closing tag \"?>\".";

  private int inlineHTMLCounter = 0;
  private boolean isOnlyClosingTag = false;
  private SyntaxToken lastInlineHTMLToken = null;

  public void initScan() {
    inlineHTMLCounter = 0;
    isOnlyClosingTag = false;
    lastInlineHTMLToken = null;
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (token.is(Kind.INLINE_HTML_TOKEN)) {
      inlineHTMLCounter++;
      isOnlyClosingTag = CheckUtils.isClosingTag(token);
      lastInlineHTMLToken = token;
    }
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    initScan();

    super.visitCompilationUnit(tree);

    if (inlineHTMLCounter == 1 && isOnlyClosingTag) {
      context().newIssue(this, lastInlineHTMLToken, MESSAGE);
    }
  }
}
