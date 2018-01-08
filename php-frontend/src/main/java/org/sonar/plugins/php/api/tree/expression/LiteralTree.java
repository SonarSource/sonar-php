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
package org.sonar.plugins.php.api.tree.expression;

import com.google.common.annotations.Beta;
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
@Beta
public interface LiteralTree extends ExpressionTree {

  SyntaxToken token();

  String value();

}
