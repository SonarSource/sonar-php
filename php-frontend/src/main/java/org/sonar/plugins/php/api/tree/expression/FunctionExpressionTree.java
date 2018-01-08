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
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;

import javax.annotation.Nullable;

/**
 * <p><a href="http://php.net/manual/en/functions.anonymous.php">Anonymous Function</a>
 * <pre>
 *  function {@link #parameters()} {@link #body()}
 *  function {@link #parameters()} {@link #lexicalVars()} {@link #body()}
 *  function & {@link #parameters()} {@link #body()}
 *  static function {@link #parameters()} {@link #body()}
 * </pre>
 */
@Beta
public interface FunctionExpressionTree extends FunctionTree, ExpressionTree {

  @Nullable
  SyntaxToken staticToken();

  @Override
  SyntaxToken functionToken();

  @Nullable
  @Override
  SyntaxToken referenceToken();

  @Override
  ParameterListTree parameters();

  @Nullable
  LexicalVariablesTree lexicalVars();

  @Override
  @Nullable
  ReturnTypeClauseTree returnTypeClause();

  @Override
  BlockTree body();

}
