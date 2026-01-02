/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.plugins.php.api.tree.statement;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Represents <a href="http://php.net/manual/en/control-structures.if.php">if statement</a> and alternative if statement syntax as well.
 * <pre>
 *   if {@link #condition()} {@link #statements()}
 *   if {@link #condition()} : {@link #statements()} endif ;    // alternative syntax
 *   if {@link #condition()} {@link #statements()} {@link #elseifClauses()} {@link #elseClause()}
 * </pre>
 */
public interface IfStatementTree extends StatementTree {

  SyntaxToken ifToken();

  ParenthesisedExpressionTree condition();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

  List<ElseifClauseTree> elseifClauses();

  @Nullable
  ElseClauseTree elseClause();

  @Nullable
  SyntaxToken endifToken();

  @Nullable
  SyntaxToken eosToken();

}
