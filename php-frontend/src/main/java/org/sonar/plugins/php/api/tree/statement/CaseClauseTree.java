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
package org.sonar.plugins.php.api.tree.statement;

import java.util.List;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Case clause in <a href="http://php.net/manual/en/control-structures.switch.php">switch statement</a> (see {@link SwitchStatementTree}).
 * <pre>
 *   case {@link #expression()} : {@link #statements()}
 *   case {@link #expression()} ;
 *   case {@link #expression()} ; {@link #statements()}
 * </pre>
 */
public interface CaseClauseTree extends SwitchCaseClauseTree {

  @Override
  SyntaxToken caseToken();

  ExpressionTree expression();

  /**
   * Either {@link PHPPunctuator#COLON :} or {@link PHPPunctuator#SEMICOLON ;}
   */
  @Override
  SyntaxToken caseSeparatorToken();

  @Override
  List<StatementTree> statements();

}
