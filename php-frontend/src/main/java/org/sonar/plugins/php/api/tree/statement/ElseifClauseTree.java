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
package org.sonar.plugins.php.api.tree.statement;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/control-structures.elseif.php">Else-if clause</a>
 * of <a href="http://php.net/manual/en/control-structures.if.php">if statement</a> (see {@link IfStatementTree}).
 * <pre>
 *   elseif {@link #condition()} : {@link #statements()}   // here {@link #statements()} can contain any number of elements
 *   elseif {@link #condition()} {@link #statements()}   // here {@link #statements()} should contain one element
 * </pre>
 */
public interface ElseifClauseTree extends StatementTree {

  SyntaxToken elseifToken();

  ParenthesisedExpressionTree condition();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

}
