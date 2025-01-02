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
package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.Tree;

/**
 * This interface represents class statement, which can be:
 * <ul>
 *   <li>{@link Kind#METHOD_DECLARATION Method declaration}
 *   <li>{@link Kind#CLASS_PROPERTY_DECLARATION Class variable declaration}
 *   <li>{@link Kind#USE_TRAIT_DECLARATION Trait use statement}
 *   <li>{@link Kind#ENUM_CASE Enum case for enum declarations}
 * <ul/>
 */
public interface ClassMemberTree extends Tree {
}
