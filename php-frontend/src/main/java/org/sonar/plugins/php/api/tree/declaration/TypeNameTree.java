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
package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.Tree;

/**
 * This interface represents type clause (appearing in function return type, parameter type or class property type (since PHP 7.4),
 * which can be:
 * <ul>
 *   <li>{@link Kind#BUILT_IN_TYPE} see {@link BuiltInTypeTree}
 *   <li>{@link Kind#NAMESPACE_NAME} for custom class or interface type
 * <ul/>
 */
public interface TypeNameTree extends Tree {
}
