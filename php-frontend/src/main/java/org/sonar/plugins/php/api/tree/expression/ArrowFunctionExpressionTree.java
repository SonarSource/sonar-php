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

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public interface ArrowFunctionExpressionTree extends FunctionTree, ExpressionTree {

  @Nullable
  SyntaxToken staticToken();

  @Override
  // "fn" token
  SyntaxToken functionToken();

  @Nullable
  @Override
  SyntaxToken referenceToken();

  @Override
  ParameterListTree parameters();

  @Override
  @Nullable
  ReturnTypeClauseTree returnTypeClause();

  SyntaxToken doubleArrowToken();

  @Override
  ExpressionTree body();

}
