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

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.namespaces.importing.php">Use namespaces</a> declaration clause
 * <pre>
 *   {@link #namespaceName()}
 *   {@link #namespaceName()} as {@link #alias()}
 *   const {@link #namespaceName()} as {@link #alias()}
 *   function {@link #namespaceName()}
 * </pre>
 */
public interface UseClauseTree extends Tree {

  @Nullable
  /**
   * Either {@link org.sonar.php.api.PHPKeyword#CONST const} or {@link org.sonar.php.api.PHPKeyword#FUNCTION function}
   */
  SyntaxToken useTypeToken();

  NamespaceNameTree namespaceName();

  @Nullable
  SyntaxToken asToken();

  @Nullable
  NameIdentifierTree alias();
}
