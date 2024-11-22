/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
 *   {@link #leftOperand()} {@link Tree.Kind#MULTIPLY *} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#DIVIDE /} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#REMAINDER %} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#PLUS +} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#MINUS -} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#LEFT_SHIFT <<} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#RIGHT_SHIFT >>} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#LESS_THAN <} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#GREATER_THAN >} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#LESS_THAN_OR_EQUAL_TO <=} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#GREATER_THAN_OR_EQUAL_TO >=} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#EQUAL_TO ==} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#STRICT_EQUAL_TO ===} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#NOT_EQUAL_TO !=} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#STRICT_NOT_EQUAL_TO !==} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#ALTERNATIVE_NOT_EQUAL_TO <>} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#BITWISE_AND &} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#BITWISE_XOR ^} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#BITWISE_OR |} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#CONDITIONAL_AND &&} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#ALTERNATIVE_CONDITIONAL_AND and} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#CONDITIONAL_OR ||} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#ALTERNATIVE_CONDITIONAL_OR or} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#INSTANCE_OF instanceof} {@link #rightOperand()}
 *   {@link #leftOperand()} {@link Tree.Kind#CONCATENATION .} {@link #rightOperand()}
 * </pre>
 */
public interface BinaryExpressionTree extends ExpressionTree {

  ExpressionTree leftOperand();

  SyntaxToken operator();

  ExpressionTree rightOperand();

}
