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
package org.sonar.plugins.php.api.tree.expression;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Conditional Expression
 * <pre>
 *   {@link #condition()} ? : {@link #falseExpression()}
 *   {@link #condition()} ? {@link #trueExpression()} : {@link #falseExpression()}
 * </pre>
 */
public interface ConditionalExpressionTree extends ExpressionTree {

  ExpressionTree condition();

  SyntaxToken queryToken();

  @Nullable
  ExpressionTree trueExpression();

  SyntaxToken colonToken();

  ExpressionTree falseExpression();

}
