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
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = MissingNewLineAtEOFCheck.KEY)
public class MissingNewLineAtEOFCheck extends PHPVisitorCheck {

  public static final String KEY = "S113";
  private static final String MESSAGE = "Add a new line at the end of this file.";

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    SyntaxToken eofToken = tree.eofToken();
    if (eofToken.column() != 0 || eofToken.line() == 1) {
      context().newFileIssue(this, MESSAGE);
    }
  }
}
