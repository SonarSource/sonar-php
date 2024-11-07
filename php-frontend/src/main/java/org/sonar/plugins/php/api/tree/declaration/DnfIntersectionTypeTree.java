/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Represent intersection in Disjunctive Normal Form (DNF) of types.
 * <pre>
 *  {@link DnfTypeTree}
 * </pre>
 *
 * @since 3.39
 */
public interface DnfIntersectionTypeTree extends DeclaredTypeTree {

  /**
   * The open parenthesis token, e.g., <code>(</code> in <code>(int|string)</code>.
   * @return the open parenthesis token
   */
  SyntaxToken openParenthesisToken();

  /**
   * The list of elements and separators, e.g., <code>int</code>, <code>|</code> and <code>string</code> in <code>(int|string)</code>.
   * @return the list of elements and separators
   */
  SeparatedList<TypeTree> types();

  /**
   * The closed parenthesis token, e.g., <code>)</code> in <code>(int|string)</code>.
   * @return the closed parenthesis token
   */
  SyntaxToken closedParenthesisToken();
}