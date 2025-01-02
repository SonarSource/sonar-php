/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.plugins.php.api.tree.expression;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/functions.user-defined.php">Function call</a>
 * <pre>
 *   {@link #callee()} {@link #arguments()}
 *   {@link #callee()} ( {@link #arguments()} )
 * </pre>
 */
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

  SeparatedList<CallArgumentTree> callArguments();

  /**
   * Nullable in case of internal function call with no parenthesis
   */
  @Nullable
  SyntaxToken closeParenthesisToken();

}
