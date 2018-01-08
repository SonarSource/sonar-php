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
package org.sonar.plugins.php.api.tree.statement;

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents <a href="http://php.net/manual/en/control-structures.foreach.php">foreach statement</a> and alternative foreach statement syntax as well.
 *
 * <pre>
 *   foreach ( {@link #expression()} as {@link #key()} => {@link #value()} ) {@link #statement()}
 *   foreach ( {@link #expression()} as {@link #key()} ) {@link #statement()}
 *   foreach ( {@link #expression()} as {@link #key()} ) : {@link #statement()} endforeach ;    // alternative syntax
 * </pre>
 */
@Beta
public interface ForEachStatementTree extends StatementTree {

  SyntaxToken foreachToken();

  SyntaxToken openParenthesisToken();

  ExpressionTree expression();

  SyntaxToken asToken();

  /**
   * value can be:
   * <ul>
   *   <li>{@link Kind#REFERENCE_VARIABLE reference variable}
   *   <li>{@link Kind#LIST_EXPRESSION assignment list }
   *   <li> or other expressions.
   * <ul/>
   */
  @Nullable
  ExpressionTree key();

  @Nullable
  SyntaxToken doubleArrowToken();

  /**
   * value can be:
   * <ul>
   *   <li>{@link Kind#REFERENCE_VARIABLE reference variable}
   *   <li>{@link Kind#LIST_EXPRESSION list expression}
   *   <li> or other expressions.
   * <ul/>
   */
  ExpressionTree value();

  SyntaxToken closeParenthesisToken();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

  @Nullable
  SyntaxToken endforeachToken();

  @Nullable
  SyntaxToken eosToken();

}
