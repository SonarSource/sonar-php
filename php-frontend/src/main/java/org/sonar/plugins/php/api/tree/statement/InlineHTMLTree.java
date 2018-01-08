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
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * This interface presents HTML code embedded into php code.
 * <p>NOTE! If you want consider all HTML code in the tree you should use {@link SyntaxToken} with kind {@link Kind#INLINE_HTML_TOKEN}.
 * <p> In the following code both "?> &lt;/a> <?php" are presented as {@link Kind#INLINE_HTML_TOKEN} but in first line this token is part of assignment statement
 * ({@link ExpressionStatementTree#eosToken()}).
 * <pre>
 *   ... $a = 5 ?> &lt;/a> <?php ...   // "?> &lt;/a> <?php" is considered as end of assignment statement
 *   ... $a = 5; ?> &lt;/a> <?php ...   // ";" is considered as end of assignment statement and "?> &lt;/a> <?php" is considered as instance of {@link InlineHTMLTree}
 * </pre>
 */
@Beta
public interface InlineHTMLTree extends StatementTree {

  SyntaxToken inlineHTMLToken();

}
