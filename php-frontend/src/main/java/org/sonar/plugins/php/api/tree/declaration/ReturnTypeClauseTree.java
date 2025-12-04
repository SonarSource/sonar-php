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

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * ReturnTypeClauseTree is an optional element of function declaration
 * <pre>
 *  : {@link #type()}
 *  </pre>
 */
public interface ReturnTypeClauseTree extends Tree {

  SyntaxToken colonToken();

  /**
   * @deprecated since 3.11 - Use {@link #declaredType()} instead.
   */
  @Deprecated
  TypeTree type();

  DeclaredTypeTree declaredType();
}
