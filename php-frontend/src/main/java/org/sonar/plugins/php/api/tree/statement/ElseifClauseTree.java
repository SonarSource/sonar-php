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
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;
import java.util.List;

/**
 * <a href="http://php.net/manual/en/control-structures.elseif.php">Else-if clause</a>
 * of <a href="http://php.net/manual/en/control-structures.if.php">if statement</a> (see {@link IfStatementTree}).
 * <pre>
 *   elseif {@link #condition()} : {@link #statements()}   // here {@link #statements()} can contain any number of elements
 *   elseif {@link #condition()} {@link #statements()}   // here {@link #statements()} should contain one element
 * </pre>
 */
@Beta
public interface ElseifClauseTree extends StatementTree {

  SyntaxToken elseifToken();

  ParenthesisedExpressionTree condition();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

}

