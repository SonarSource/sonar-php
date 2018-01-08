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
package org.sonar.plugins.php.api.tree.declaration;

import com.google.common.annotations.Beta;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/functions.arguments.php">Function Argument</a>
 * <pre>
 *   {@link #variableIdentifier()}
 *
 *   {@link #variableIdentifier()} = {@link #initValue()}
 *   ... {@link #variableIdentifier()}
 *   & {@link #variableIdentifier()}
 *   {@link #type()} {@link #variableIdentifier()}
 * </pre>
 *
 */
@Beta
public interface ParameterTree extends Tree {

  @Nullable
  TypeTree type();

  @Nullable
  SyntaxToken referenceToken();

  @Nullable
  SyntaxToken ellipsisToken();

  VariableIdentifierTree variableIdentifier();

  @Nullable
  SyntaxToken equalToken();

  @Nullable
  ExpressionTree initValue();

}
