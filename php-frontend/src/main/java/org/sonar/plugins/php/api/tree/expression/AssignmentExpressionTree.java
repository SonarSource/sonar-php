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

import org.sonar.plugins.php.api.tree.Tree;

/**
 * <a href="http://php.net/manual/fa/language.operators.assignment.php">Assignment Expression</a>
 * <pre>
 *   {@link #variable()} {@link Tree.Kind#ASSIGNMENT =} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#POWER_ASSIGNMENT **=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#MULTIPLY_ASSIGNMENT *=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#DIVIDE_ASSIGNMENT /=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#REMAINDER_ASSIGNMENT %=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#PLUS_ASSIGNMENT +=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#MINUS_ASSIGNMENT -=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#LEFT_SHIFT_ASSIGNMENT <<=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#RIGHT_SHIFT_ASSIGNMENT >>=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#AND_ASSIGNMENT &=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#XOR_ASSIGNMENT ^=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#OR_ASSIGNMENT |=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#CONCATENATION_ASSIGNMENT .=} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#ASSIGNMENT_BY_REFERENCE =&} {@link #value()}
 *   {@link #variable()} {@link Tree.Kind#NULL_COALESCING_ASSIGNMENT ??=} {@link #value()}
 * </pre>
 */
public interface AssignmentExpressionTree extends ExpressionTree {

  ExpressionTree variable();

  String operator();

  ExpressionTree value();

}
