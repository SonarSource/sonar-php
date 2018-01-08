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
@Beta
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
