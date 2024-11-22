/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/control-structures.declare.php">Declare statement</a>
 * <pre>
 *   declare ( {@link #directives()} ) {@link #statements()}   // here list {@link #statements()} should contain only one element
 *   declare ( {@link #directives()} ) : {@link #statements()} enddeclare ; // here list {@link #statements()} can contain any number of elements
 *   declare ( {@link #directives()} ) ;
 * </pre>
 */
public interface DeclareStatementTree extends StatementTree {

  SyntaxToken declareToken();

  SyntaxToken openParenthesisToken();

  SeparatedList<VariableDeclarationTree> directives();

  SyntaxToken closeParenthesisToken();

  @Nullable
  SyntaxToken colonToken();

  List<StatementTree> statements();

  @Nullable
  SyntaxToken endDeclareToken();

  @Nullable
  SyntaxToken eosToken();
}
