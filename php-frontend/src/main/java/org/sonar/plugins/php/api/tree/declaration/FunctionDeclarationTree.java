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
package org.sonar.plugins.php.api.tree.declaration;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

/**
 * <p><a href="http://php.net/manual/en/functions.user-defined.php">Function Declaration</a>
 * <pre>
 *  function {@link #name()} {@link #parameters()} {@link #body()}
 *  function & {@link #name()} {@link #parameters()} {@link #body()}
 * </pre>
 */
public interface FunctionDeclarationTree extends FunctionTree, StatementTree {

  @Override
  SyntaxToken functionToken();

  @Nullable
  @Override
  SyntaxToken referenceToken();

  NameIdentifierTree name();

  @Override
  ParameterListTree parameters();

  @Override
  @Nullable
  ReturnTypeClauseTree returnTypeClause();

  @Override
  BlockTree body();

}
