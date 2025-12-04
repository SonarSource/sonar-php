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
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Represents <a href="">for statement</a> and alternative for statement syntax as well.
 * <pre>
 *   for ( {@link #init()} ; {@link #condition()} ; {@link #update()} ) {@link #statements()}
 *   for ( {@link #init()} ; {@link #condition()} ; {@link #update()} ) : {@link #statements()} endfor ;
 * </pre>
 */
public interface ForStatementTree extends StatementTree {

  SyntaxToken forToken();

  SyntaxToken openParenthesisToken();

  SeparatedList<ExpressionTree> init();

  SyntaxToken firstSemicolonToken();

  SeparatedList<ExpressionTree> condition();

  SyntaxToken secondSemicolonToken();

  SeparatedList<ExpressionTree> update();

  SyntaxToken closeParenthesisToken();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

  @Nullable
  SyntaxToken endforToken();

  @Nullable
  SyntaxToken eosToken();

}
