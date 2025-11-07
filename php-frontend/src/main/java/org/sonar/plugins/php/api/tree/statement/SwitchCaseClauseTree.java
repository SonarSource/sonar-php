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
package org.sonar.plugins.php.api.tree.statement;

import java.util.List;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * General interface for <a href="http://php.net/manual/en/control-structures.switch.php">switch</a> clauses.
 * Parent interface for {@link CaseClauseTree} and {@link DefaultClauseTree}.
 */
public interface SwitchCaseClauseTree extends Tree {

  SyntaxToken caseToken();

  /**
   * Either {@link PHPPunctuator#COLON :} or {@link PHPPunctuator#SEMICOLON ;}
   */
  SyntaxToken caseSeparatorToken();

  List<StatementTree> statements();

}
