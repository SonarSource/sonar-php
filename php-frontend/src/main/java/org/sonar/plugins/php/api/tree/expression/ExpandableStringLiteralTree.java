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

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import java.util.List;

/**
 * <a href="http://php.net/manual/en/language.types.string.php">Expandable String</a>, containing variable name
 * <pre>
 *   "{@link #strings() string characters} {@link #expressions() $expression}"
 *   "{@link #expressions()}"
 * </pre>
 */
@Beta
public interface ExpandableStringLiteralTree extends ExpressionTree {

  SyntaxToken openDoubleQuoteToken();

  List<ExpandableStringCharactersTree> strings();

  /**
   * Expressions can be:
   * <ul>
   *   <li>{@link Kind#EXPANDABLE_STRING_CHARACTERS expandable string characters}
   *   <li>{@link Kind#VARIABLE_IDENTIFIER variable identifier}
   *   <li>{@link Kind#COMPUTED_VARIABLE_NAME computed variable name}
   *   <li>{@link Kind#COMPOUND_VARIABLE_NAME compound variable name}
   *   <li>{@link Kind#ARRAY_ACCESS array access}
   *   <li>{@link Kind#COMPOUND_VARIABLE_NAME member access}
   * </ul>
   */
  List<ExpressionTree> expressions();

  SyntaxToken closeDoubleQuoteToken();

}
