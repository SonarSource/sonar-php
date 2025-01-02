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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;

/**
 * <a href="http://php.net/manual/en/language.namespaces.php">Namespace</a> definition
 * <pre>
 * namespace {@link #namespaceName()} ;
 * namespace {@link #namespaceName()} { {@link #statements()} }
 * </pre>
 */
public interface NamespaceStatementTree extends StatementTree {

  InternalSyntaxToken namespaceToken();

  @Nullable
  NamespaceNameTree namespaceName();

  @Nullable
  InternalSyntaxToken openCurlyBrace();

  List<StatementTree> statements();

  @Nullable
  InternalSyntaxToken closeCurlyBrace();

  @Nullable
  InternalSyntaxToken eosToken();

}
