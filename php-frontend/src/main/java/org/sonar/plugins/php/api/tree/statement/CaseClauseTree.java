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
package org.sonar.plugins.php.api.tree.statement;

import com.google.common.annotations.Beta;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import java.util.List;

/**
 * Case clause in <a href="http://php.net/manual/en/control-structures.switch.php">switch statement</a> (see {@link SwitchStatementTree}).
 * <pre>
 *   case {@link #expression()} : {@link #statements()}
 *   case {@link #expression()} ;
 *   case {@link #expression()} ; {@link #statements()}
 * </pre>
 */
@Beta
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
