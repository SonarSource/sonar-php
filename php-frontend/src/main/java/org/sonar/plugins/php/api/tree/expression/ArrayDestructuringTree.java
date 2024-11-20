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
package org.sonar.plugins.php.api.tree.expression;

import java.util.List;
import java.util.Optional;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

// This interface extends ExpressionTree only because AssignmentExpressionTree wants its left-hand side to be an ExpressionTree
public interface ArrayDestructuringTree extends ExpressionTree {

  List<Optional<ArrayAssignmentPatternElementTree>> elements();

  List<SyntaxToken> separators();

}
