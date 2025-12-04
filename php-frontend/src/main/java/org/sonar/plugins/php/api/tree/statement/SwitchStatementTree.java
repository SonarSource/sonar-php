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
package org.sonar.plugins.php.api.tree.statement;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/control-structures.switch.php">Switch statement</a>
 * <pre>
 *   switch {@link #expression()} { {@link #cases()} } }
 *   switch {@link #expression()} : {@link #cases()} endswitch ;
 * </pre>
 */
public interface SwitchStatementTree extends StatementTree {

  SyntaxToken switchToken();

  ParenthesisedExpressionTree expression();

  @Nullable
  SyntaxToken openCurlyBraceToken();

  @Nullable
  SyntaxToken colonToken();

  @Nullable
  SyntaxToken semicolonToken();

  List<SwitchCaseClauseTree> cases();

  @Nullable
  SyntaxToken closeCurlyBraceToken();

  @Nullable
  SyntaxToken endswitchToken();

  @Nullable
  SyntaxToken eosToken();
}
