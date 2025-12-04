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

import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * Expression statement is used for processing <a href="http://php.net/manual/en/language.expressions.php">expressions</a>, e.g. function call or assignment operations.
 * <pre>
 *   {@link #expression()} ;
 * </pre>
 * Examples:
 * <pre>
 *   foo() ;
 *   $a = 1 ;
 * </pre>
 */
public interface ExpressionStatementTree extends StatementTree {

  ExpressionTree expression();

  SyntaxToken eosToken();

}
