/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.utils.type;

import java.util.function.Predicate;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;

public abstract class TreeKindPredicate<T> implements Predicate<TreeValues> {

  private final Tree.Kind kind;

  public TreeKindPredicate(Tree.Kind kind) {
    this.kind = kind;
  }

  @Override
  public boolean test(TreeValues possibleValues) {
    for (ExpressionTree tree : possibleValues.values) {
      if (tree.is(kind) && matches(possibleValues, (T) tree)) {
        return true;
      }
    }
    return false;
  }

  protected abstract boolean matches(TreeValues possibleValues, T tree);

}
