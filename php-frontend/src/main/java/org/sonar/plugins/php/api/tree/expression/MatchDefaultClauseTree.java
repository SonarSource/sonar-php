/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.api.tree.expression;

import javax.annotation.Nullable;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Default match clause in <a href="https://wiki.php.net/rfc/match_expression_v2">match expression</a> (see {@link MatchClauseTree}).
 * <pre>
 *   default => {@link #expression()}
 * </pre>
 */
public interface MatchDefaultClauseTree extends MatchClauseTree {

  SyntaxToken defaultToken();

  @Nullable
  SyntaxToken trailingComma();

  /**
   * {@link PHPPunctuator#DOUBLEARROW =>}
   */
  @Override
  SyntaxToken doubleArrowToken();

  @Override
  ExpressionTree expression();
}
