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

import java.util.List;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * A BlockTree is a list of zero or more {@link #statements()} between braces which can be used anywhere a single statement is allowed.
 *
 * <pre>
 * { {@link #statements()} }
 * </pre>
 */
public interface BlockTree extends StatementTree {

  SyntaxToken openCurlyBraceToken();

  List<StatementTree> statements();

  SyntaxToken closeCurlyBraceToken();
}
