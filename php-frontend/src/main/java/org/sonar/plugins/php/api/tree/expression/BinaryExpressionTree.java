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
 * </pre>
 */
@Beta
public interface BinaryExpressionTree extends ExpressionTree {

  ExpressionTree leftOperand();

  SyntaxToken operator();

  ExpressionTree rightOperand();

}
