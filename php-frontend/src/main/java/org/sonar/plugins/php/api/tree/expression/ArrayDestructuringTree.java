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
package org.sonar.plugins.php.api.tree.expression;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Optional;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

// This interface extends ExpressionTree only because AssignmentExpressionTree wants its left-hand side to be an ExpressionTree
@Beta
public interface ArrayDestructuringTree extends ExpressionTree {

  List<Optional<ArrayAssignmentPatternElementTree>> elements();

  List<SyntaxToken> separators();

}
