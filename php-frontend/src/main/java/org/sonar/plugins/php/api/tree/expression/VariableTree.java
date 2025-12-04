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

/**
 * <a href="http://php.net/manual/en/language.variables.php">Common interface to represent variable name expression</a>
 * <pre>
 *   ${ {@link #variableExpression()} } // {@link Kind#COMPOUND_VARIABLE_NAME COMPOUND_VARIABLE_NAME}
 *   { {@link #variableExpression()} }  // {@link Kind#COMPOUND_VARIABLE_NAME COMPOUND_VARIABLE_NAME}
 *   & {@link #variableExpression()}    // {@link Kind#COMPUTED_VARIABLE_NAME COMPUTED_VARIABLE_NAME}
 *   $$ {@link #variableExpression()}   // {@link Kind#VARIABLE_VARIABLE VARIABLE_VARIABLE}
 *   {@link #variableExpression()}      // {@link Kind#VARIABLE_IDENTIFIER VARIABLE_IDENTIFIER}
 * </pre>
 */
public interface VariableTree extends ExpressionTree {

  ExpressionTree variableExpression();

}
