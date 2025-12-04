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
package org.sonar.plugins.php.api.tree.expression;

import org.sonar.plugins.php.api.tree.SeparatedList;

/**
 * Common interface for <a href="http://php.net/manual/en/language.types.array.php">Array</a> Initializer
 * <ul>
 *   <li>{@link Kind#ARRAY_INITIALIZER_BRACKET Array initializer bracket}
 *   <li>{@link Kind#ARRAY_INITIALIZER_FUNCTION Array initializer function}
 * <ul/>
 */
public interface ArrayInitializerTree extends ExpressionTree {

  SeparatedList<ArrayPairTree> arrayPairs();

}
