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

import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import com.google.common.annotations.Beta;

/**
 * <a href="http://php.net/manual/en/language.types.type-juggling.php">Cast Expression</a>
 * <pre>
 *   ( {@link #castType()} ) {@link #expression()}
 * </pre>
 */
@Beta
public interface CastExpressionTree extends ExpressionTree {

  SyntaxToken openParenthesisToken();

  /**
   * The casts allowed in PHP are:
   * <ul>
   *   <li>{@link PHPKeyword#ARRAY array},
   *   <li>{@link PHPKeyword#UNSET unset} - cast to <a href="http://php.net/manual/en/language.types.null.php">NULL</a>
   *   <li>int, integer - cast to <a href="http://php.net/manual/en/language.types.integer.php">integer</a>
   *   <li>float, double, real  - cast to <a href="http://php.net/manual/en/language.types.float.php">float</a>
   *   <li>string - cast to <a href="http://php.net/manual/en/language.types.string.php">string</a>
   *   <li>object - cast to <a href="http://php.net/manual/en/language.types.object.php">object</a>
   *   <li>bool, boolean - cast to <a href="http://php.net/manual/en/language.types.boolean.php">boolean</a>
   *   <li>binary - cast to binary strings
   */
  SyntaxToken castType();

  SyntaxToken closeParenthesisToken();

  ExpressionTree expression();
}
