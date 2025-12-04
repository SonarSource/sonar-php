/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.exceptions.php">Try statement</a>
 * <pre>
 *   try {@link #block()} {@link #catchBlocks()} finally {@link #finallyBlock()}
 *   try {@link #block()} {@link #catchBlocks()}
 * </pre>
 */
public interface TryStatementTree extends StatementTree {

  SyntaxToken tryToken();

  BlockTree block();

  List<CatchBlockTree> catchBlocks();

  @Nullable
  SyntaxToken finallyToken();

  @Nullable
  BlockTree finallyBlock();

}
