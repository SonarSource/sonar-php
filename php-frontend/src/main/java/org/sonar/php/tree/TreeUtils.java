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
package org.sonar.php.tree;

import java.util.Collection;
import javax.annotation.CheckForNull;
import org.sonar.plugins.php.api.tree.Tree;

public class TreeUtils {

  private TreeUtils() {
    // utility class - do not instantiate
  }

  public static boolean isDescendant(Tree tree, Tree potentialParent) {
    Tree parent = tree;
    while (parent != null && !potentialParent.equals(parent)) {
      parent = parent.getParent();
    }
    return potentialParent.equals(parent);
  }

  @CheckForNull
  public static Tree findAncestorWithKind(Tree tree, Collection<Tree.Kind> kinds) {
    Tree parent = tree;
    while (parent != null && !kinds.contains(parent.getKind())) {
      parent = parent.getParent();
    }
    return parent;
  }
}
