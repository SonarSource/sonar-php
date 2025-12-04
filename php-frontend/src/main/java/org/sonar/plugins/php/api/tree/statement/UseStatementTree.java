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
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.namespaces.importing.php">Use namespaces</a> declaration
 * <p>Corresponds to {@link Tree.Kind#USE_STATEMENT}</p>
 * <pre>
 *   use {@link #clauses()} ;
 * </pre>
 *
 * <p><a href="http://php.net/manual/en/language.oop5.properties.php">Use Function</a> declaration
 * <p>Corresponds to {@link Tree.Kind#USE_STATEMENT}</p>
 * <pre>
 *   use function {@link #clauses()} ;
 * </pre>
 *
 * <p><a href="http://php.net/manual/en/language.oop5.constants.php">Use Constant</a> declaration
 * <p>Corresponds to {@link Tree.Kind#USE_STATEMENT}</p>
 * <pre>
 *   use const {@link #clauses()} ;
 * </pre>
 *
 * <p><a href="http://php.net/manual/en/language.namespaces.importing.php#language.namespaces.importing.group">Group Use</a> declaration
 * <p>Corresponds to {@link Tree.Kind#GROUP_USE_STATEMENT}</p>
 * <pre>
 *   use {@link #prefix()} \ { {@link #clauses()} } ;
 *   use const {@link #prefix()} \ { {@link #clauses()} } ;
 *   use function {@link #prefix()} \ { {@link #clauses()} } ;
 * </pre>
 */
public interface UseStatementTree extends StatementTree {

  SyntaxToken useToken();

  /**
   * Either {@link org.sonar.php.api.PHPKeyword#CONST const} or {@link org.sonar.php.api.PHPKeyword#FUNCTION function}
   */
  @Nullable
  SyntaxToken useTypeToken();

  @Nullable
  NamespaceNameTree prefix();

  @Nullable
  SyntaxToken nsSeparatorToken();

  @Nullable
  SyntaxToken openCurlyBraceToken();

  SeparatedList<UseClauseTree> clauses();

  @Nullable
  SyntaxToken closeCurlyBraceToken();

  @Nullable
  SyntaxToken eosToken();
}
