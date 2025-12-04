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
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;

/**
 * <p><a href="http://php.net/manual/en/functions.anonymous.php">Anonymous Function</a>
 * <pre>
 *  function {@link #parameters()} {@link #body()}
 *  function {@link #parameters()} {@link #lexicalVars()} {@link #body()}
 *  function & {@link #parameters()} {@link #body()}
 *  static function {@link #parameters()} {@link #body()}
 * </pre>
 */
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
