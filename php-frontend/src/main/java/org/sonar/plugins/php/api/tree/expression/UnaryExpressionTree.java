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
package org.sonar.plugins.php.api.tree.expression;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <pre>
 *   {@link #expression()} {@link Tree.Kind#POSTFIX_INCREMENT ++}
 *   {@link #expression()} {@link Tree.Kind#POSTFIX_DECREMENT --}
 *   {@link Tree.Kind#ERROR_CONTROL @} {@link #expression()}
 *   {@link Tree.Kind#PREFIX_DECREMENT --} {@link #expression()}
 *   {@link Tree.Kind#PREFIX_INCREMENT ++} {@link #expression()}
 *   {@link Tree.Kind#UNARY_PLUS +} {@link #expression()}
 *   {@link Tree.Kind#UNARY_MINUS -} {@link #expression()}
 *   {@link Tree.Kind#BITWISE_COMPLEMENT ~} {@link #expression()}
 *   {@link Tree.Kind#LOGICAL_COMPLEMENT !} {@link #expression()}
 * </pre>
 */
public interface UnaryExpressionTree extends ExpressionTree {

  SyntaxToken operator();

  ExpressionTree expression();

}
