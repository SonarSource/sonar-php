/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.api.tree.declaration;

import java.util.List;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;

/**
 * <a href="https://wiki.php.net/rfc/property-hooks">Property Hooks</a>
 *
 * @since 3.39
 */
public interface PropertyHookListTree extends Tree {

  /**
   * The open curly brace beginning the declaration of property hooks.
   */
  InternalSyntaxToken openCurlyBrace();

  /**
   * The list of {@link PropertyHookTree property hooks}
   */
  List<PropertyHookTree> hooks();

  /**
   * The closed curly brace ending the declaration of property hooks.
   */
  InternalSyntaxToken closeCurlyBrace();
}
