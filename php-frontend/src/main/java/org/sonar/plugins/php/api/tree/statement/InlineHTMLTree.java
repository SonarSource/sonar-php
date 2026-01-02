/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
public interface InlineHTMLTree extends StatementTree {

  SyntaxToken inlineHTMLToken();

}
