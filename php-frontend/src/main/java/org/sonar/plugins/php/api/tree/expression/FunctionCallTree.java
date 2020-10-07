/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.declaration.FunctionCallArgumentTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;

/**
 * <a href="http://php.net/manual/en/functions.user-defined.php">Function call</a>
 * <pre>
 *   {@link #callee()} {@link #arguments()}
 *   {@link #callee()} ( {@link #arguments()} )
 * </pre>
 */
@Beta
public interface FunctionCallTree extends ExpressionTree {

  ExpressionTree callee();

  /**
   * Nullable in case of internal function call with no parenthesis
   */
  @Nullable
  SyntaxToken openParenthesisToken();

  /**
   * @deprecated since 3.11 . Use {@link #callArguments()} instead.
   */
  @Deprecated
  SeparatedList<ExpressionTree> arguments();

  SeparatedList<FunctionCallArgumentTree> callArguments();

  /**
   * Retrieves an argument based on position and name.
   *
   * If an argument with the given name exists, it is returned no matter the position.
   * Else, the argument at the supplied position is returned if it exists and is not named.
   *
   * @since 3.11
   */
  @Nullable
  FunctionCallArgumentTree argument(int position, String name);

  /**
   * Nullable in case of internal function call with no parenthesis
   */
  @Nullable
  SyntaxToken closeParenthesisToken();

}
