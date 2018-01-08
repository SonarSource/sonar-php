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
@Beta
public interface VariableTree extends ExpressionTree {

  ExpressionTree variableExpression();

}
