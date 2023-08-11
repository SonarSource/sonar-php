/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.api.tree.statement;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Represents <a href="http://php.net/manual/en/control-structures.while.php">while statement</a> and alternative while statement syntax as well.
 * <pre>
 *   while {@link #condition()} {@link #statement()}
 *   while {@link #condition()} : {@link #statement()} endwhile ;    // alternative syntax
 * </pre>
 */
public interface WhileStatementTree extends StatementTree {

  SyntaxToken whileToken();

  ParenthesisedExpressionTree condition();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

  @Nullable
  SyntaxToken endWhileToken();

  @Nullable
  SyntaxToken eosToken();

}
