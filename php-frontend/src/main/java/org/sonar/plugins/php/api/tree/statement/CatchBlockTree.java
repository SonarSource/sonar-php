/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Catch block of <a href="http://php.net/manual/en/language.exceptions.php">try statement</a> (see {@link TryStatementTree}).
 * <pre>
 *   catch ( {@link #exceptionTypes()} {@link #variable()} ) {@link #block()}
 * </pre>
 */
public interface CatchBlockTree extends Tree {

  SyntaxToken catchToken();

  SyntaxToken openParenthesisToken();

  SeparatedList<NamespaceNameTree> exceptionTypes();

  @Nullable
  VariableIdentifierTree variable();

  SyntaxToken closeParenthesisToken();

  BlockTree block();

}
