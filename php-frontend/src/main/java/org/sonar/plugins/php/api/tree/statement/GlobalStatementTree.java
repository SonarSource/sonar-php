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

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.variables.scope.php#language.variables.scope.global">Global</a> variable declaration
 * <pre>
 *   global {@link #variables()} ;
 * </pre>
 */
public interface GlobalStatementTree extends StatementTree {

  SyntaxToken globalToken();

  /**
   * Members can be:
   * <ul>
   *   <li>{@link Kind#VARIABLE_IDENTIFIER variable identifier}
   *   <li>{@link Kind#VARIABLE_VARIABLE variable variable}
   *   <li>{@link Kind#COMPOUND_VARIABLE_NAME compound variable name}
   * <ul/>
   */
  SeparatedList<VariableTree> variables();

  SyntaxToken eosToken();

}
