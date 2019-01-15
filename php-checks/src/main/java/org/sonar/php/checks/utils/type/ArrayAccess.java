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
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

public class ArrayAccess implements Predicate<TreeValues> {

  private final TypePredicateList nestedPredicates;

  public ArrayAccess(Predicate<TreeValues>... nestedPredicates) {
    this.nestedPredicates = new TypePredicateList(nestedPredicates);
  }

  @Override
  public boolean test(TreeValues possibleValues) {
    for (ExpressionTree tree : possibleValues.values) {
      if ((tree.is(Tree.Kind.ARRAY_ACCESS) && matches(possibleValues, (ArrayAccessTree) tree)) ||
        (tree.getParent().is(Tree.Kind.FOREACH_STATEMENT) && matches(possibleValues, tree, (ForEachStatementTree) tree.getParent()))) {
        return true;
      }
    }
    return false;
  }

  protected boolean matches(TreeValues possibleValues, ArrayAccessTree arrayAccess) {
    return nestedPredicates.test(possibleValues.lookupPossibleValues(arrayAccess.object()));
  }

  protected boolean matches(TreeValues possibleValues, ExpressionTree value, ForEachStatementTree forEachStatementTree) {
    return forEachStatementTree.value() == value && nestedPredicates.test(possibleValues.lookupPossibleValues(forEachStatementTree.expression()));
  }

}
