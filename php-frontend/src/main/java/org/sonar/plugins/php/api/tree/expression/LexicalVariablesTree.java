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
package org.sonar.plugins.php.api.tree.expression;

import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/functions.anonymous.php">Lexical Variable</a>: use construct that allows
 * an anonymous function to inherit from parent scope variables.
 * <pre>
 *   use ( {@link #variables()} )
 * </pre>
 *
 */
public interface LexicalVariablesTree extends ExpressionTree {

  SyntaxToken useToken();

  SyntaxToken openParenthesisToken();

  /**
   * Variables can be:
   * <ul>
   *   <li>{@link Kind#REFERENCE_VARIABLE Reference variable}
   *   <li>{@link Kind#VARIABLE_IDENTIFIER Variable identifier}
   * <ul/>
   */
  SeparatedList<VariableTree> variables();

  SyntaxToken closeParenthesisToken();

}
