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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import java.util.List;

/**
 * General interface for <a href="http://php.net/manual/en/control-structures.switch.php">switch</a> clauses.
 * Parent interface for {@link CaseClauseTree} and {@link DefaultClauseTree}.
 */
@Beta
public interface SwitchCaseClauseTree extends Tree {

  SyntaxToken caseToken();

  /**
   * Either {@link PHPPunctuator#COLON :} or {@link PHPPunctuator#SEMICOLON ;}
   */
  SyntaxToken caseSeparatorToken();

  List<StatementTree> statements();

}
