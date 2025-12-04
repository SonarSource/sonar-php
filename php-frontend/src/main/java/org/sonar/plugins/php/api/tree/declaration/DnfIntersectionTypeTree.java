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
