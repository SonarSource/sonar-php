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

import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Literal
 *
 * <pre>
 *   {@link Kind#NULL_LITERAL null}
 *   {@link Kind#BOOLEAN_LITERAL true}               // boolean
 *   {@link Kind#NUMERIC_LITERAL 0}                  // numeric
 *   {@link Kind#REGULAR_STRING_LITERAL "regular string"}   // string without embedded variable
 *   {@link Kind#HEREDOC_LITERAL heredoc}
 *   {@link Kind#HEREDOC_LITERAL __CLASS__}          // magic constant
 * </pre>
 */
public interface LiteralTree extends ExpressionTree {

  SyntaxToken token();

  String value();

}
